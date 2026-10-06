package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShaukusMinion.class, ZhalfirinKnight.class, FeralShadow.class, Pacifism.class, Incinerate.class})
class ShaukusMinionTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 2 damage to target white creature, killing a 2/2")
    void abilityKillsWhiteCreature() {
        Permanent minion = addCreatureReady(player1, new ShaukusMinion());
        Permanent target = addCreatureReady(player2, new ZhalfirinKnight());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(minion.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Zhalfirin Knight");
        harness.assertInGraveyard(player2, "Zhalfirin Knight");
    }

    @Test
    @DisplayName("Cannot target a non-white creature")
    void cannotTargetNonWhiteCreature() {
        addCreatureReady(player1, new ShaukusMinion());
        Permanent shadow = addCreatureReady(player2, new FeralShadow());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shadow.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white creature");
    }

    @Test
    @DisplayName("Cannot target a white noncreature permanent")
    void cannotTargetWhiteNonCreaturePermanent() {
        addCreatureReady(player1, new ShaukusMinion());
        Permanent pacifism = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, pacifism.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Can target a white creature controlled by the ability controller")
    void canTargetOwnWhiteCreature() {
        addCreatureReady(player1, new ShaukusMinion());
        Permanent target = addCreatureReady(player1, new ZhalfirinKnight());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertOnBattlefield(player1, "Zhalfirin Knight");
        assertThat(target.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Zhalfirin Knight");
        harness.assertInGraveyard(player1, "Zhalfirin Knight");
    }

    @Test
    @DisplayName("Tap cost prevents activating a tapped Minion")
    void cannotActivateWhileTapped() {
        Permanent minion = addCreatureReady(player1, new ShaukusMinion());
        minion.setTapped(true);
        Permanent target = addCreatureReady(player2, new ZhalfirinKnight());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tap cost prevents activating a summoning-sick Minion")
    void cannotActivateWithSummoningSickness() {
        Permanent minion = addCreatureReady(player1, new ShaukusMinion());
        minion.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new ZhalfirinKnight());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(minion.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Black mana cannot replace the red component of the activation cost")
    void requiresRedMana() {
        Permanent minion = addCreatureReady(player1, new ShaukusMinion());
        Permanent target = addCreatureReady(player2, new ZhalfirinKnight());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(minion.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Red mana cannot replace the black component of the activation cost")
    void requiresBlackMana() {
        Permanent minion = addCreatureReady(player1, new ShaukusMinion());
        Permanent target = addCreatureReady(player2, new ZhalfirinKnight());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(minion.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activated ability still deals damage after Minion is destroyed in response")
    void abilityResolvesAfterSourceIsDestroyed() {
        Permanent minion = addCreatureReady(player1, new ShaukusMinion());
        Permanent target = addCreatureReady(player2, new ZhalfirinKnight());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player2, 0, minion.getId());

        harness.assertInGraveyard(player1, "Shauku's Minion");
        harness.assertOnBattlefield(player2, "Zhalfirin Knight");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Zhalfirin Knight");
        harness.assertInGraveyard(player2, "Zhalfirin Knight");
    }
}
