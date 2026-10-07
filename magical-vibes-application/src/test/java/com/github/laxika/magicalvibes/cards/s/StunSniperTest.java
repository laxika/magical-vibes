package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StunSniper.class, GrizzlyBears.class, FugitiveWizard.class, Forest.class})
class StunSniperTest extends BaseCardTest {

    @Test
    @DisplayName("Activating taps the sniper and puts the ability on the stack targeting the creature")
    void activatingPutsOnStack() {
        Permanent sniper = addCreatureReady(player1, new StunSniper());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(sniper.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving deals 1 damage to the target creature and taps it")
    void dealsDamageAndTapsTarget() {
        addCreatureReady(player1, new StunSniper());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("1 damage kills a 1-toughness creature")
    void killsOneToughnessCreature() {
        addCreatureReady(player1, new StunSniper());
        Permanent target = addCreatureReady(player2, new FugitiveWizard());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new StunSniper());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new StunSniper());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("An already tapped target still takes damage")
    void damagesAlreadyTappedCreature() {
        addCreatureReady(player1, new StunSniper());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target a creature controlled by the sniper's controller")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new StunSniper());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability resolves after its source leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent sniper = addCreatureReady(player1, new StunSniper());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(sniper);
        gd.playerGraveyards.get(player1.getId()).add(sniper.getCard());

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped sniper cannot activate its ability")
    void cannotActivateTappedSniper() {
        Permanent sniper = addCreatureReady(player1, new StunSniper());
        sniper.tap();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning sick sniper cannot activate its ability")
    void cannotActivateSummoningSickSniper() {
        harness.addToBattlefield(player1, new StunSniper());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability does nothing when its target leaves before resolution")
    void doesNothingWithoutTarget() {
        addCreatureReady(player1, new StunSniper());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.isTapped()).isFalse();
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }
}
