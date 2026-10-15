package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YawgmothsWill;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnderworldRageHound.class, GrizzlyBears.class, YawgmothsWill.class})
class UnderworldRageHoundTest extends BaseCardTest {

    @Test
    void castFromHandEntersWithoutCounter() {
        harness.setHand(player1, List.of(new UnderworldRageHound()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Underworld Rage-Hound")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void escapingExilesThreeOtherCardsAndAddsCounter() {
        UnderworldRageHound rageHound = new UnderworldRageHound();
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(rageHound, first, second, third));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Underworld Rage-Hound")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void escapeRequiresThreeOtherCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(
                new UnderworldRageHound(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void normalGraveyardCastDoesNotEscapeWithCounter() {
        harness.setHand(player1, List.of(new YawgmothsWill()));
        harness.setGraveyard(player1, List.of(new UnderworldRageHound()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Underworld Rage-Hound")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void escapeCounterIsPresentAsCreatureEnters() {
        harness.setGraveyard(player1, List.of(new UnderworldRageHound(),
                new UnderworldRageHound(), new UnderworldRageHound(), new UnderworldRageHound()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Underworld Rage-Hound")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Underworld Rage-Hound").isEscaped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void escapeCannotExileTheCardBeingCast() {
        UnderworldRageHound rageHound = new UnderworldRageHound();
        harness.setGraveyard(player1, List.of(rageHound,
                new UnderworldRageHound(), new UnderworldRageHound(), new UnderworldRageHound()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rageHound).hasSize(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void escapeCannotExileTheSameCardTwice() {
        harness.setGraveyard(player1, List.of(new UnderworldRageHound(),
                new UnderworldRageHound(), new UnderworldRageHound(), new UnderworldRageHound()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void escapeCannotUseTheCheaperPrintedManaCost() {
        harness.setGraveyard(player1, List.of(new UnderworldRageHound(),
                new UnderworldRageHound(), new UnderworldRageHound(), new UnderworldRageHound()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void escapeStillRequiresCreatureSpellTiming() {
        harness.setGraveyard(player1, List.of(new UnderworldRageHound(),
                new UnderworldRageHound(), new UnderworldRageHound(), new UnderworldRageHound()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void tappedCreatureIsNotRequiredToAttack() {
        Permanent rageHound = addCreatureReady(player1, new UnderworldRageHound());
        rageHound.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(rageHound.isAttacking()).isFalse();
    }

    @Test
    void summoningSickCreatureIsNotRequiredToAttack() {
        Permanent rageHound = harness.addToBattlefieldAndReturn(player1, new UnderworldRageHound());
        rageHound.setSummoningSick(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(rageHound.isAttacking()).isFalse();
    }

    @Test
    void mustAttackWhenAble() {
        Permanent rageHound = addCreatureReady(player1, new UnderworldRageHound());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }
}
