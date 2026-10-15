package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BronzeCudgels;
import com.github.laxika.magicalvibes.cards.s.ShortCircuit;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WalkingSkyscraper.class, GrizzlyBears.class, Shock.class, BronzeCudgels.class, ShortCircuit.class})
class WalkingSkyscraperTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each modified creature you control")
    void costsLessForEachModifiedCreatureYouControl() {
        Permanent modifiedCreature = addCreatureReady(player1, new GrizzlyBears());
        modifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new WalkingSkyscraper()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Only modified creatures you control reduce the cost")
    void onlyModifiedCreaturesYouControlReduceTheCost() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new WalkingSkyscraper()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Has hexproof while untapped and loses it when tapped")
    void hasHexproofOnlyWhileUntapped() {
        Permanent skyscraper = addCreatureReady(player1, new WalkingSkyscraper());

        assertThat(gqs.hasKeyword(gd, skyscraper, Keyword.HEXPROOF)).isTrue();

        skyscraper.tap();

        assertThat(gqs.hasKeyword(gd, skyscraper, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Untapped Walking Skyscraper cannot be targeted")
    void untappedSkyscraperCannotBeTargeted() {
        Permanent skyscraper = addCreatureReady(player2, new WalkingSkyscraper());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, skyscraper.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    void multipleModificationsOnOneCreatureCountOnlyOnce() {
        Permanent creature = addCreatureReady(player1, new WalkingSkyscraper());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new BronzeCudgels());
        equipment.setAttachedTo(creature.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ShortCircuit());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new WalkingSkyscraper()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void equipmentAndControlledAuraEachModifyTheirCreature() {
        Permanent equipped = addCreatureReady(player1, new WalkingSkyscraper());
        Permanent enchanted = addCreatureReady(player1, new WalkingSkyscraper());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new BronzeCudgels());
        equipment.setAttachedTo(equipped.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ShortCircuit());
        aura.setAttachedTo(enchanted.getId());
        harness.setHand(player1, List.of(new WalkingSkyscraper()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentAuraAndCountersOnNoncreatureDoNotReduceCost() {
        Permanent creature = addCreatureReady(player1, new WalkingSkyscraper());
        creature.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new ShortCircuit());
        aura.setAttachedTo(creature.getId());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new BronzeCudgels());
        equipment.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new WalkingSkyscraper()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void negativeCountersAlsoModifyCreatures() {
        Permanent creature = addCreatureReady(player1, new WalkingSkyscraper());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new WalkingSkyscraper()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void costCannotBeReducedBelowZero() {
        for (int i = 0; i < 9; i++) {
            Permanent creature = addCreatureReady(player1, new WalkingSkyscraper());
            creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        }
        harness.setHand(player1, List.of(new WalkingSkyscraper()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void controllerCanTargetUntappedSkyscraper() {
        Permanent skyscraper = addCreatureReady(player1, new WalkingSkyscraper());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, skyscraper.getId());

        assertThat(skyscraper.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void opponentCanTargetTappedSkyscraper() {
        Permanent skyscraper = addCreatureReady(player2, new WalkingSkyscraper());
        skyscraper.tap();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, skyscraper.getId());

        assertThat(skyscraper.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void untappingBeforeResolutionMakesOpponentsTargetIllegal() {
        Permanent skyscraper = addCreatureReady(player2, new WalkingSkyscraper());
        skyscraper.tap();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, skyscraper.getId());

        skyscraper.untap();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(skyscraper.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void trampleDealsExcessDamageThroughBlocker() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new WalkingSkyscraper());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 6));

        harness.assertLife(player2, 14);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Walking Skyscraper");
    }
}