package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ChampionOfLambholt;
import com.github.laxika.magicalvibes.cards.d.DarksteelColossus;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EldraziConfluence.class, AirElemental.class, DarksteelColossus.class, Forest.class,
        Spellbook.class})
class EldraziConfluenceTest extends BaseCardTest {

    @Test
    void resolvesAllThreeModes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        cast(new int[]{0, 1, 2}, List.of(creature.getId(), artifact.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(findPermanents(player2, "Spellbook")).hasSize(1)
                .allMatch(Permanent::isTapped);
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
    }

    @Test
    void repeatedModesCanCreateMultipleScionsAndScionAddsColorlessMana() {
        cast(new int[]{2, 2, 2}, List.of());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(3);

        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(2);
    }

    @Test
    void repeatedCreatureModeCanTargetTheSameCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DarksteelColossus());

        cast(new int[]{0, 0, 0}, List.of(creature.getId(), creature.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(20);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void repeatedCreatureModesKeepTheirSeparateTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DarksteelColossus());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        cast(new int[]{0, 0, 0}, List.of(first.getId(), second.getId(), first.getId()));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(17);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(1);
    }

    @Test
    void flickerModeRejectsLandTargets() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> cast(new int[]{1, 1, 1}, List.of(forest.getId(), forest.getId(), forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({ChampionOfLambholt.class})
    void differentModesResolveInPrintedOrderRegardlessOfSelectionOrder() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new ChampionOfLambholt());

        cast(new int[]{2, 2, 1}, List.of(champion.getId()));
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Champion of Lambholt");
        assertThat(returned.getId()).isNotEqualTo(champion.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(2);
    }

    @Test
    void allTargetsBecomingIllegalPreventsUntargetedTokenModes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        cast(new int[]{0, 2, 2}, List.of(creature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerHands.get(player2.getId()).add(creature.getCard());

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        harness.assertInGraveyard(player1, "Eldrazi Confluence");
    }

    @Test
    void oneIllegalTargetDoesNotPreventOtherModes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        cast(new int[]{0, 1, 2}, List.of(creature.getId(), artifact.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerHands.get(player2.getId()).add(creature.getCard());

        resolveAllTriggers();

        assertThat(findPermanent(player2, "Spellbook").getId()).isNotEqualTo(artifact.getId());
        assertThat(findPermanent(player2, "Spellbook").isTapped()).isTrue();
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
    }

    @Test
    void flickerReturnsStolenPermanentToItsOwner() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        gd.stolenCreatures.put(artifact.getId(), player2.getId());

        cast(new int[]{1, 2, 2}, List.of(artifact.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spellbook")).isEmpty();
        assertThat(findPermanent(player2, "Spellbook").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Spellbook").getId()).isNotEqualTo(artifact.getId());
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(2);
    }

    @Test
    void flickeringScionTokenDoesNotReturnIt() {
        cast(new int[]{2, 2, 2}, List.of());
        resolveAllTriggers();
        Permanent scion = findPermanent(player1, "Eldrazi Scion");

        cast(new int[]{1, 1, 1}, List.of(scion.getId(), scion.getId(), scion.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(2)
                .noneMatch(permanent -> permanent.getId().equals(scion.getId()));
    }

    private void cast(int[] modeIndices, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new EldraziConfluence()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelection(3, modeIndices),
                null, null, targetIds, List.of());
    }
}
