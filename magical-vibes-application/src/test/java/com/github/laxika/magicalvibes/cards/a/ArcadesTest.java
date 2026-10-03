package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.o.OreskosSwiftclaw;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.w.WallOfVines;
import com.github.laxika.magicalvibes.cards.d.DeepFreeze;
import com.github.laxika.magicalvibes.cards.g.GuardDuty;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Arcades.class, WallOfVines.class, OreskosSwiftclaw.class, GreenwoodSentinel.class})
class ArcadesTest extends BaseCardTest {

    @Test
    @DisplayName("Defender creatures assign combat damage equal to toughness")
    void defenderCreaturesUseToughnessForCombatDamage() {
        harness.addToBattlefield(player1, new Arcades());
        Permanent wall = addCreatureReady(player1, new WallOfVines());
        Permanent swiftclaw = addCreatureReady(player1, new OreskosSwiftclaw());

        assertThat(gqs.getEffectiveCombatDamage(gd, wall)).isEqualTo(3);
        assertThat(gqs.getEffectiveCombatDamage(gd, swiftclaw)).isEqualTo(3);
    }

    @Test
    @DisplayName("Defender creatures can attack")
    void defenderCreaturesCanAttack() {
        harness.addToBattlefield(player1, new Arcades());
        Permanent wall = addCreatureReady(player1, new WallOfVines());
        harness.addToBattlefield(player2, new GreenwoodSentinel());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(wall)));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Draws when a defender creature enters under its controller's control")
    void drawsWhenDefenderEnters() {
        harness.addToBattlefield(player1, new Arcades());
        harness.setHand(player1, List.of(new WallOfVines()));
        harness.setLibrary(player1, List.of(new GreenwoodSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Greenwood Sentinel");
    }

    @Test
    @DisplayName("Does not draw when a non-defender creature enters")
    void doesNotDrawWhenNonDefenderEnters() {
        harness.addToBattlefield(player1, new Arcades());
        harness.setHand(player1, List.of(new GreenwoodSentinel()));
        harness.setLibrary(player1, List.of(new WallOfVines()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent's defenders do not use toughness for combat damage")
    void opponentDefenderUsesPower() {
        harness.addToBattlefield(player1, new Arcades());
        Permanent wall = addCreatureReady(player2, new WallOfVines());

        assertThat(gqs.getEffectiveCombatDamage(gd, wall)).isZero();
    }

    @Test
    @DisplayName("An opposing defender entering does not draw a card")
    void opponentDefenderDoesNotTriggerDraw() {
        harness.addToBattlefield(player1, new Arcades());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GreenwoodSentinel()));
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new WallOfVines()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Defender damage reverts to power when Arcades leaves")
    void damageRevertsWhenArcadesLeaves() {
        Permanent arcades = harness.addToBattlefieldAndReturn(player1, new Arcades());
        Permanent wall = addCreatureReady(player1, new WallOfVines());
        declareAttackers(player1, List.of(1));

        gd.playerBattlefields.get(player1.getId()).remove(arcades);

        assertThat(wall.isAttacking()).isTrue();
        assertThat(gqs.getEffectiveCombatDamage(gd, wall)).isZero();
    }

    @Test
    @CardUsed({DeepFreeze.class})
    @DisplayName("Arcades losing its abilities stops the toughness damage effect")
    void losingAbilitiesStopsToughnessDamage() {
        Permanent arcades = harness.addToBattlefieldAndReturn(player1, new Arcades());
        Permanent wall = addCreatureReady(player1, new WallOfVines());
        harness.setHand(player1, List.of(new DeepFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, arcades.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectiveCombatDamage(gd, wall)).isZero();
    }

    @Test
    @CardUsed({DeepFreeze.class})
    @DisplayName("Arcades losing its abilities stops defender attack permission")
    void losingAbilitiesStopsAttackPermission() {
        Permanent arcades = harness.addToBattlefieldAndReturn(player1, new Arcades());
        addCreatureReady(player1, new WallOfVines());
        harness.setHand(player1, List.of(new DeepFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, arcades.getId());
        resolveAllTriggers();

        assertThatThrownBy(() -> declareAttackers(player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("An opponent's defender cannot attack using Arcades")
    void opponentDefenderCannotAttack() {
        harness.addToBattlefield(player1, new Arcades());
        addCreatureReady(player2, new WallOfVines());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The draw trigger resolves after the entering defender leaves")
    void drawTriggerSurvivesDefenderLeaving() {
        harness.addToBattlefield(player1, new Arcades());
        harness.setHand(player1, List.of(new WallOfVines()));
        harness.setLibrary(player1, List.of(new GreenwoodSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent wall = findPermanent(player1, "Wall of Vines");
        gd.playerBattlefields.get(player1.getId()).remove(wall);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Greenwood Sentinel");
    }

    @Test
    @CardUsed({GuardDuty.class})
    @DisplayName("Arcades itself uses toughness if it gains defender")
    void arcadesWithDefenderUsesToughness() {
        Permanent arcades = harness.addToBattlefieldAndReturn(player1, new Arcades());
        harness.setHand(player1, List.of(new GuardDuty()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, arcades.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectiveCombatDamage(gd, arcades)).isEqualTo(5);
    }

    @Test
    @DisplayName("An attacking defender deals damage equal to toughness")
    void attackingDefenderDealsToughnessDamage() {
        harness.addToBattlefield(player1, new Arcades());
        addCreatureReady(player1, new WallOfVines());

        declareAttackersAndPrepareBlockers(player1, List.of(1));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("A blocking defender deals damage equal to toughness")
    void blockingDefenderDealsToughnessDamage() {
        addCreatureReady(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player2, new Arcades());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfVines());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        harness.assertOnBattlefield(player2, "Wall of Vines");
        assertThat(wall.getMarkedDamage()).isEqualTo(2);
    }
}
