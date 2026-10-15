package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FieryFall;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShamblingRemains.class, Unsummon.class, FieryFall.class})
@DisplayName("Shambling Remains")
class ShamblingRemainsTest extends BaseCardTest {

    @Test
    @DisplayName("Shambling Remains cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent remains = harness.addToBattlefieldAndReturn(player2, new ShamblingRemains());
        remains.setSummoningSick(false);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ShamblingRemains());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Unearth returns Shambling Remains to the battlefield with haste")
    void unearthReturnsWithHaste() {
        harness.setGraveyard(player1, List.of(new ShamblingRemains()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Shambling Remains");
        assertThat(perm.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Shambling Remains");
    }

    @Test
    @DisplayName("Unearthed Shambling Remains is exiled at the next end step")
    void unearthExiledAtEndStep() {
        harness.setGraveyard(player1, List.of(new ShamblingRemains()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Shambling Remains");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shambling Remains"));
    }

    @Test
    @DisplayName("Unearthed Shambling Remains can attack immediately")
    void unearthedCreatureCanAttackImmediately() {
        unearthRemains();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(findPermanent(player1, "Shambling Remains").isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Unearth cannot be activated during an opponent's turn")
    void cannotUnearthDuringOpponentsTurn() {
        prepareUnearth();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Shambling Remains");
        harness.assertNotOnBattlefield(player1, "Shambling Remains");
    }

    @Test
    @DisplayName("Unearth cannot be activated outside a main phase")
    void cannotUnearthOutsideMainPhase() {
        prepareUnearth();
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertInGraveyard(player1, "Shambling Remains");
    }

    @Test
    @DisplayName("Unearth cannot be activated while the stack is nonempty")
    void cannotUnearthWithNonemptyStack() {
        prepareUnearth();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ShamblingRemains());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, creature.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.assertInGraveyard(player1, "Shambling Remains");
    }

    @Test
    @DisplayName("Unearth requires red mana as well as black mana")
    void cannotUnearthWithoutRedMana() {
        harness.setGraveyard(player1, List.of(new ShamblingRemains()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Shambling Remains");
        harness.assertNotOnBattlefield(player1, "Shambling Remains");
    }

    @Test
    @DisplayName("Returning an unearthed creature to hand exiles it instead")
    void unearthedCreatureIsExiledInsteadOfReturningToHand() {
        Permanent remains = unearthRemains();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, remains.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shambling Remains");
        harness.assertNotInHand(player1, "Shambling Remains");
        harness.assertNotInGraveyard(player1, "Shambling Remains");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(remains.getCard().getId()));
    }

    @Test
    @DisplayName("Lethal damage to an unearthed creature exiles it instead of putting it in the graveyard")
    void unearthedCreatureIsExiledInsteadOfDying() {
        Permanent remains = unearthRemains();
        harness.setHand(player1, List.of(new FieryFall()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castInstant(player1, 0, remains.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shambling Remains");
        harness.assertNotInGraveyard(player1, "Shambling Remains");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(remains.getCard().getId()));
    }

    private void prepareUnearth() {
        harness.setGraveyard(player1, List.of(new ShamblingRemains()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private Permanent unearthRemains() {
        prepareUnearth();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Shambling Remains");
    }
}
