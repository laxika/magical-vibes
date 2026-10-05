package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PromiseOfLoyalty.class, GrizzlyBears.class, ChandraNalaar.class})
class PromiseOfLoyaltyTest extends BaseCardTest {

    @Test
    @DisplayName("Each player keeps one creature, puts a vow counter on it, and sacrifices the rest")
    void keepsOneCreaturePerPlayer() {
        Permanent ownKept = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownSacrificed = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingKept = addCreatureReady(player2, new GrizzlyBears());
        Permanent opposingSacrificed = addCreatureReady(player2, new GrizzlyBears());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownKept.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(opposingKept.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ownKept);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposingKept);
        assertThat(ownKept.getCounterCount(CounterType.VOW)).isEqualTo(1);
        assertThat(opposingKept.getCounterCount(CounterType.VOW)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownSacrificed.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingSacrificed.getCard());
    }

    @Test
    @DisplayName("A kept creature cannot attack the spell's controller")
    void keptCreatureCannotAttackController() {
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        cast();

        assertThat(opposingCreature.getCounterCount(CounterType.VOW)).isEqualTo(1);
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A kept creature cannot attack the controller's planeswalker")
    void keptCreatureCannotAttackControllerPlaneswalker() {
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);

        cast();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0),
                Map.of(0, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opposingCreature.getCounterCount(CounterType.VOW)).isEqualTo(1);
    }

    @Test
    @DisplayName("Moving a vow counter to an unchosen creature does not restrict it")
    void movedVowCounterDoesNotRestrictNewBearer() {
        Permanent chosen = addCreatureReady(player2, new GrizzlyBears());
        cast();
        Permanent newBearer = addCreatureReady(player2, new GrizzlyBears());

        chosen.setCounterCount(CounterType.VOW, 0);
        newBearer.setCounterCount(CounterType.VOW, 1);

        declareAttackers(player2, List.of(1));
        assertThat(newBearer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing the last vow counter allows the chosen creature to attack")
    void removingVowCounterEndsRestriction() {
        Permanent chosen = addCreatureReady(player2, new GrizzlyBears());
        cast();
        chosen.setCounterCount(CounterType.VOW, 0);

        declareAttackers(player2, List.of(0));
        assertThat(chosen.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An ended restriction does not restart when a vow counter returns")
    void restoringVowCounterDoesNotRestartRestriction() {
        Permanent chosen = addCreatureReady(player2, new GrizzlyBears());
        cast();
        chosen.setCounterCount(CounterType.VOW, 0);
        chosen.setCounterCount(CounterType.VOW, 1);

        declareAttackers(player2, List.of(0));
        assertThat(chosen.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A second caster gains their own protection from chosen creatures")
    void secondCasterAlsoReceivesProtection() {
        addCreatureReady(player1, new GrizzlyBears());
        cast();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new PromiseOfLoyalty()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.castAndResolveSorcery(player2, 0, 0);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature with an old vow counter is sacrificed unless chosen again")
    void oldVowCounterDoesNotSaveUnchosenCreature() {
        Permanent oldBearer = addCreatureReady(player2, new GrizzlyBears());
        oldBearer.setCounterCount(CounterType.VOW, 1);
        Permanent chosen = addCreatureReady(player2, new GrizzlyBears());

        cast();
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(oldBearer.getCard());
        assertThat(chosen.getCounterCount(CounterType.VOW)).isEqualTo(1);
    }

    @Test
    @DisplayName("The spell resolves when neither player controls a creature")
    void resolvesWithEmptyBattlefields() {
        cast();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Promise of Loyalty");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
    private void cast() {
        harness.setHand(player1, List.of(new PromiseOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

}
