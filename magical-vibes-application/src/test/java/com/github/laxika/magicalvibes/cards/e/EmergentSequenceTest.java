package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PrismariCampus;
import com.github.laxika.magicalvibes.cards.r.RecklessAmplimancer;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmergentSequence.class, Forest.class, RecklessAmplimancer.class, PrismariCampus.class,
        SoulWarden.class})
class EmergentSequenceTest extends BaseCardTest {

    @Test
    void searchesForABasicLandAnimatesItAndCountsLandsEnteredThisTurn() {
        harness.setHand(player1, List.of(new EmergentSequence()));
        harness.setLibrary(player1, List.of(new Forest(), new RecklessAmplimancer()));
        gd.permanentsEnteredBattlefieldThisTurn.put(player1.getId(),
                new ArrayList<>(List.of(new Forest(), new RecklessAmplimancer())));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> gqs.isLand(gd, permanent))
                .findFirst()
                .orElseThrow();
        assertThat(forest.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(forest.getEffectivePower()).isEqualTo(2);
        assertThat(forest.getEffectiveToughness()).isEqualTo(2);
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(forest.getGrantedSubtypes()).contains(CardSubtype.FRACTAL);
        assertThat(gqs.getEffectiveColors(gd, forest))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
        assertThat(gqs.isLand(gd, forest)).isTrue();
    }

    @Test
    void mayFailToFindABasicLand() {
        harness.setHand(player1, List.of(new EmergentSequence()));
        harness.setLibrary(player1, List.of(new RecklessAmplimancer()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void countsItsOwnLandButNotLandsEnteringUnderOpponentControl() {
        harness.enterBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new EmergentSequence()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(forest.getEffectivePower()).isEqualTo(1);
        assertThat(forest.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotFindANonbasicLand() {
        PrismariCampus campus = new PrismariCampus();
        harness.setHand(player1, List.of(new EmergentSequence()));
        harness.setLibrary(player1, List.of(campus));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(campus);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void fetchedLandDoesNotTriggerCreatureEntryAbilities() {
        harness.addToBattlefield(player2, new SoulWarden());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new EmergentSequence()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gqs.isCreature(gd, gd.playerBattlefields.get(player1.getId()).getFirst())).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void canDeclineToFindEvenWhenBasicLandIsAvailable() {
        Forest existingForest = new Forest();
        harness.addToBattlefield(player1, existingForest);
        harness.setHand(player1, List.of(new EmergentSequence()));
        Forest libraryForest = new Forest();
        harness.setLibrary(player1, List.of(libraryForest));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryForest);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void animationPersistsAcrossTurnsAndRetainsForestManaAbility() {
        harness.setHand(player1, List.of(new EmergentSequence()));
        harness.setLibrary(player1, List.of(new Forest(), new RecklessAmplimancer(),
                new RecklessAmplimancer()));
        harness.setLibrary(player2, List.of(new RecklessAmplimancer(), new RecklessAmplimancer()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(forest.getEffectivePower()).isEqualTo(1);
        assertThat(forest.getEffectiveToughness()).isEqualTo(1);
        assertThat(forest.getGrantedSubtypes()).contains(CardSubtype.FRACTAL);
        assertThat(gqs.getEffectiveColors(gd, forest))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
        assertThat(forest.isTapped()).isFalse();
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
