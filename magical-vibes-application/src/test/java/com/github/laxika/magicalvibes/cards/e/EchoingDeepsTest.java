package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CosmiumConfluence;
import com.github.laxika.magicalvibes.cards.c.CrucibleOfWorlds;
import com.github.laxika.magicalvibes.cards.h.HourOfPromise;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SunkenCitadel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EchoingDeeps.class, Forest.class, SunkenCitadel.class, CosmiumConfluence.class, CrucibleOfWorlds.class,
        HourOfPromise.class, Plains.class})
class EchoingDeepsTest extends BaseCardTest {

    @Test
    void entersUntappedWhenThereAreNoLandCardsInGraveyards() {
        harness.setHand(player1, List.of(new EchoingDeeps()));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayEnterTappedAsCopyOfLandCardInAnyGraveyard() {
        Card forest = new Forest();
        harness.setHand(player1, List.of(new EchoingDeeps()));
        harness.setGraveyard(player2, List.of(forest));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        Permanent entered = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(entered.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(forest);

        entered.untap();
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void decliningCopyChoiceMakesItEnterUntapped() {
        harness.setHand(player1, List.of(new EchoingDeeps()));
        harness.setGraveyard(player2, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void uncopiedLandProducesColorlessMana() {
        harness.setHand(player1, List.of(new EchoingDeeps()));
        harness.setGraveyard(player1, List.of(new CosmiumConfluence()));
        harness.setGraveyard(player2, List.of());

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningCopyRetainsColorlessManaAbility() {
        harness.setHand(player1, List.of(new EchoingDeeps()));
        harness.setGraveyard(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void copyingOwnLandAddsCaveAndLeavesOriginalInGraveyard() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new EchoingDeeps()));
        harness.setGraveyard(player1, List.of(new CosmiumConfluence(), forest));

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        Permanent entered = findPermanent(player1, "Forest");
        assertThat(entered.isTapped()).isTrue();
        assertThat(gqs.effectiveLandTypes(gd, entered)).contains(CardSubtype.FOREST, CardSubtype.CAVE);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copiedLandMakesItsAsEntersChoiceAndUsesItsManaAbility() {
        harness.setHand(player1, List.of(new EchoingDeeps()));
        SunkenCitadel citadel = new SunkenCitadel();
        harness.setGraveyard(player2, List.of(citadel));

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(citadel.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        Permanent entered = findPermanent(player1, "Sunken Citadel");
        assertThat(entered.isTapped()).isTrue();
        assertThat(entered.getChosenColor()).isEqualTo(CardColor.BLUE);
        assertThat(gqs.effectiveLandTypes(gd, entered)).containsOnly(CardSubtype.CAVE);
        entered.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void landPutOntoBattlefieldByAnEffectStillOffersCopyChoice() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(new EchoingDeeps()));
        harness.setGraveyard(player2, List.of(forest));
        harness.setHand(player1, List.of(new CosmiumConfluence()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, ChooseOneEffect.encodeRepeatedModeSelection(3, 0, 0, 0));
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        Permanent entered = findPermanent(player1, "Forest");
        assertThat(entered.isTapped()).isTrue();
        assertThat(gqs.effectiveLandTypes(gd, entered)).contains(CardSubtype.FOREST, CardSubtype.CAVE);
        assertThat(gd.landsPlayedThisTurn.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Cosmium Confluence");
    }

    @Test
    void copyChoiceFinishesBeforeTheRestOfTheSpellResolves() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(new EchoingDeeps()));
        harness.setGraveyard(player2, List.of(forest));
        harness.setHand(player1, List.of(new CosmiumConfluence()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, ChooseOneEffect.encodeRepeatedModeSelection(3, 0, 1, 1));
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        Permanent entered = findPermanent(player1, "Forest");
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gqs.isCreature(gd, entered)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void playingItFromAGraveyardOffersCopyChoice() {
        EchoingDeeps deeps = new EchoingDeeps();
        Forest forest = new Forest();
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(deeps));
        harness.setGraveyard(player2, List.of(forest));

        harness.playGraveyardLand(player1, deeps.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        Permanent entered = findPermanent(player1, "Forest");
        assertThat(entered.getOriginalCard()).isSameAs(deeps);
        assertThat(entered.isTapped()).isTrue();
        assertThat(gqs.effectiveLandTypes(gd, entered)).contains(CardSubtype.FOREST, CardSubtype.CAVE);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(deeps);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void eachDeepsFetchedTogetherGetsItsOwnCopyChoice() {
        EchoingDeeps first = new EchoingDeeps();
        EchoingDeeps second = new EchoingDeeps();
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(first, second));
        harness.setGraveyard(player2, List.of(forest, plains));
        harness.castFromHand(player1, new HourOfPromise(), "{4}{G}");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(plains.getId()));

        Permanent forestCopy = findPermanent(player1, "Forest");
        Permanent plainsCopy = findPermanent(player1, "Plains");
        assertThat(List.of(forestCopy.getOriginalCard(), plainsCopy.getOriginalCard()))
                .containsExactlyInAnyOrder(first, second);
        assertThat(forestCopy.isTapped()).isTrue();
        assertThat(plainsCopy.isTapped()).isTrue();
        assertThat(gqs.effectiveLandTypes(gd, plainsCopy)).contains(CardSubtype.PLAINS, CardSubtype.CAVE);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Hour of Promise");
    }
}
