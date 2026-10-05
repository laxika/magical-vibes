package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LightningBlast;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImpsTaunt.class, TrainedArmodon.class, LightningBlast.class})
class ImpsTauntTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Imps' Taunt forces the target to attack, with no forced defender")
    void forcesTargetToAttack() {
        harness.addToBattlefield(player2, new TrainedArmodon());
        harness.setHand(player1, List.of(new ImpsTaunt()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Trained Armodon");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent target = findPermanent(player2, "Trained Armodon");
        assertThat(target.isMustAttackThisTurn()).isTrue();
        assertThat(target.getMustAttackTargetId()).isNull();
    }

    @Test
    @DisplayName("Imps' Taunt can target a creature its controller controls")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new TrainedArmodon());
        harness.setHand(player1, List.of(new ImpsTaunt()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Trained Armodon");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(findPermanent(player1, "Trained Armodon").isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Without buyback the spell goes to the graveyard as it resolves")
    void resolvesToGraveyardWithoutBuyback() {
        harness.addToBattlefield(player2, new TrainedArmodon());
        harness.setHand(player1, List.of(new ImpsTaunt()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Trained Armodon"));

        assertThat(handNames(player1)).isEmpty();
        assertThat(graveyardNames(player1)).containsExactly("Imps' Taunt");
    }

    @Test
    @DisplayName("A target that can attack cannot be omitted from attackers")
    void requiresTargetToAttackWhenAble() {
        Permanent target = addCreatureReady(player2, new TrainedArmodon());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new ImpsTaunt()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        declareAttackers(player2, List.of(0));

        assertThat(target.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("A target that cannot attack is not required to attack")
    void doesNotRequireUnableTargetToAttack() {
        Permanent target = addCreatureReady(player2, new TrainedArmodon());
        target.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new ImpsTaunt()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        declareAttackers(player2, List.of());

        assertThat(target.isAttackedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Paying buyback returns the spell to hand as it resolves")
    void buybackReturnsToHand() {
        harness.addToBattlefield(player2, new TrainedArmodon());
        harness.setHand(player1, List.of(new ImpsTaunt()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Trained Armodon");
        harness.castInstantWithBuyback(player1, 0, targetId);
        assertThat(gd.stack.getFirst().isBuyback()).isTrue();

        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Trained Armodon").isMustAttackThisTurn()).isTrue();
        assertThat(graveyardNames(player1)).doesNotContain("Imps' Taunt");
        assertThat(handNames(player1)).containsExactly("Imps' Taunt");
    }

    @Test
    @DisplayName("A fizzled buyback spell still goes to the graveyard")
    void buybackFizzleGoesToGraveyard() {
        harness.addToBattlefield(player2, new TrainedArmodon());
        harness.setHand(player1, List.of(new ImpsTaunt()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Trained Armodon");
        harness.castInstantWithBuyback(player1, 0, targetId);

        harness.setHand(player2, List.of(new LightningBlast()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        assertThat(handNames(player1)).isEmpty();
        assertThat(graveyardNames(player1)).containsExactly("Imps' Taunt");
    }

    @Test
    @DisplayName("Imps' Taunt cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new ImpsTaunt()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick target is not required to attack")
    void doesNotRequireSummoningSickTargetToAttack() {
        harness.addToBattlefield(player2, new TrainedArmodon());
        Permanent target = findPermanent(player2, "Trained Armodon");
        target.setSummoningSick(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new ImpsTaunt()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        declareAttackers(player2, List.of());

        assertThat(target.isAttackedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The attack requirement expires at the end of the turn")
    void attackRequirementExpiresAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new TrainedArmodon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ImpsTaunt()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(target.isMustAttackThisTurn()).isTrue();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(target.isMustAttackThisTurn()).isFalse();
        declareAttackers(player2, List.of());
        assertThat(target.isAttackedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Buyback cannot be paid with less than the full additional cost")
    void buybackRequiresFullAdditionalManaCost() {
        harness.addToBattlefield(player2, new TrainedArmodon());
        harness.setHand(player1, List.of(new ImpsTaunt()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantWithBuyback(player1, 0,
                harness.getPermanentId(player2, "Trained Armodon")))
                .isInstanceOf(IllegalStateException.class);

        assertThat(handNames(player1)).containsExactly("Imps' Taunt");
        assertThat(gd.stack).isEmpty();
    }

    private List<String> handNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(c -> c.getName()).toList();
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(c -> c.getName()).toList();
    }
}
