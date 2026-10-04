package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.InspiringOverseer;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.o.ObNixilisTheAdversary;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrislySigil.class, AirElemental.class, GrizzlyBears.class, Island.class, LightningBolt.class,
        InspiringOverseer.class, ObNixilisTheAdversary.class})
class GrislySigilTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage and gains 1 life against an undamaged target")
    void usesBaseEffectAgainstUndamagedTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new GrislySigil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Deals 3 damage and gains 3 life when the target was dealt noncombat damage")
    void usesUpgradedEffectAfterNoncombatDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new LightningBolt(), new GrislySigil()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(6);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Casualty copy resolves first and upgrades the original spell")
    void casualtyCopyUpgradesOriginalSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent casualty = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrislySigil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), casualty.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.setHand(player1, List.of(new GrislySigil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    @DisplayName("Noncombat damage to a planeswalker upgrades the next Sigil")
    void upgradesAgainstDamagedPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ObNixilisTheAdversary());
        target.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new GrislySigil(), new GrislySigil()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Ob Nixilis, the Adversary");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("The original gains no life if the casualty copy kills its only target")
    void originalDoesNotGainLifeAfterCopyKillsTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InspiringOverseer());
        Permanent casualty = harness.addToBattlefieldAndReturn(player1, new InspiringOverseer());
        harness.setHand(player1, List.of(new GrislySigil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), casualty.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Inspiring Overseer");
        harness.assertInGraveyard(player1, "Inspiring Overseer");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A casualty copy can target another creature without upgrading the original")
    void casualtyCopyCanChooseDifferentTarget() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new InspiringOverseer());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player1, new InspiringOverseer());
        Permanent casualty = harness.addToBattlefieldAndReturn(player1, new InspiringOverseer());
        harness.setHand(player1, List.of(new GrislySigil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.castSorceryWithSacrifice(player1, 0, originalTarget.getId(), casualty.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(originalTarget.getMarkedDamage()).isEqualTo(1);
        assertThat(copyTarget.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Inspiring Overseer");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage dealt in response is considered when Sigil resolves")
    void checksNoncombatDamageAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new GrislySigil(), new LightningBolt()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(6);
        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new GrislySigil()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevented copy damage still gains life but does not upgrade the original")
    void preventedCopyDamageDoesNotUpgradeOriginal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InspiringOverseer());
        target.setCounterCount(CounterType.SHIELD, 1);
        Permanent casualty = harness.addToBattlefieldAndReturn(player1, new InspiringOverseer());
        harness.setHand(player1, List.of(new GrislySigil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), casualty.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Inspiring Overseer");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }
}
