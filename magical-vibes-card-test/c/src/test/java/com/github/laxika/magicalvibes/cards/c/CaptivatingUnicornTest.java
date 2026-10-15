package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BronzeSword;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Ichthyomorphosis;
import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaptivatingUnicorn.class, GloriousAnthem.class, GrizzlyBears.class,
        BronzeSword.class, Ichthyomorphosis.class, NyxbornCourser.class})
class CaptivatingUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a target creature an opponent controls when your enchantment enters")
    void tapsTargetCreatureWhenEnchantmentEnters() {
        harness.addToBattlefield(player1, new CaptivatingUnicorn());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger for an enchantment entering under an opponent's control")
    void opponentEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new CaptivatingUnicorn());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot choose a creature you control")
    void cannotChooseOwnCreature() {
        harness.addToBattlefield(player1, new CaptivatingUnicorn());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    void enchantmentCreatureEnteringWithoutBeingCastTriggers() {
        harness.addToBattlefield(player1, new CaptivatingUnicorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());

        harness.enterBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void nonEnchantmentCreatureDoesNotTrigger() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new CaptivatingUnicorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());

        harness.enterBattlefieldAndReturn(player1, new CaptivatingUnicorn());
        harness.passBothPriorities();

        assertThat(unicorn.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noOpponentCreaturesLeavesNoTargetChoice() {
        harness.addToBattlefield(player1, new CaptivatingUnicorn());

        harness.enterBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotChooseOpponentsNonCreaturePermanent() {
        harness.addToBattlefield(player1, new CaptivatingUnicorn());
        Permanent sword = harness.addToBattlefieldAndReturn(player2, new BronzeSword());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());

        harness.enterBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, sword.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(sword.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void triggerResolvesAfterUnicornLeavesBattlefield() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new CaptivatingUnicorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());

        harness.enterBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(unicorn);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void targetLeavingDoesNotTapAnotherCreature() {
        harness.addToBattlefield(player1, new CaptivatingUnicorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());

        harness.enterBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(other.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unicornWithAbilitiesRemovedDoesNotTrigger() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new CaptivatingUnicorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        harness.setHand(player2, List.of(new Ichthyomorphosis()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0, unicorn.getId());
        harness.passBothPriorities();

        harness.enterBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
