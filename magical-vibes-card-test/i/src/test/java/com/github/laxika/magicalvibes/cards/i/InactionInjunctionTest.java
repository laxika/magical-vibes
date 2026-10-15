package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AxebaneGuardian;
import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.g.GolgariGuildgate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InactionInjunction.class, DrudgeBeetle.class, AxebaneGuardian.class, GolgariGuildgate.class})
class InactionInjunctionTest extends BaseCardTest {

    @Test
    @DisplayName("Detained creature can't attack")
    void detainedCreatureCannotAttack() {
        harness.addToBattlefield(player2, new DrudgeBeetle());
        Permanent bears = detain("Drudge Beetle");

        assertThatThrownBy(() -> declareAttack(bears))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Detained creature can't block")
    void detainedCreatureCannotBlock() {
        harness.addToBattlefield(player2, new DrudgeBeetle());
        detain("Drudge Beetle");

        Permanent attacker = addCreatureReady(player1, new DrudgeBeetle());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Detained creature can't activate its abilities")
    void detainedCreatureCannotActivateAbilities() {
        addCreatureReady(player2, new AxebaneGuardian());
        detain("Axebane Guardian");

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Detain wears off at the caster's next turn")
    void detainWearsOffAtControllersNextTurn() {
        harness.addToBattlefield(player2, new DrudgeBeetle());
        Permanent bears = detain("Drudge Beetle");

        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThatCode(() -> declareAttack(bears)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Controller draws a card")
    void controllerDrawsACard() {
        harness.addToBattlefield(player2, new DrudgeBeetle());
        detain("Drudge Beetle");

        // setHand replaced the hand with the single Injunction, which left it on cast; the
        // resolved draw is therefore the only card in hand.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new DrudgeBeetle());
        UUID ownBearId = harness.getPermanentId(player1, "Drudge Beetle");
        harness.setHand(player1, List.of(new InactionInjunction()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, ownBearId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new GolgariGuildgate());
        harness.setHand(player1, List.of(new InactionInjunction()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Detain does not tap the creature")
    void detainDoesNotTapCreature() {
        harness.addToBattlefield(player2, new DrudgeBeetle());

        Permanent creature = detain("Drudge Beetle");

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Detain survives the opponent's turn starting")
    void detainSurvivesOpponentsTurnStart() {
        harness.addToBattlefield(player2, new DrudgeBeetle());
        Permanent creature = detain("Drudge Beetle");

        gd.expireFloatingEffectsAtTurnStart(player2.getId());

        assertThatThrownBy(() -> declareAttack(creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("No card is drawn if the target leaves before resolution")
    void noDrawWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        harness.setHand(player1, List.of(new InactionInjunction()));
        harness.setLibrary(player1, List.of(new DrudgeBeetle()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Inaction Injunction");
    }

    @Test
    @DisplayName("No card is drawn if the caster gains control of the target before resolution")
    void noDrawWhenTargetBecomesControlledByCaster() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        harness.setHand(player1, List.of(new InactionInjunction()));
        harness.setLibrary(player1, List.of(new DrudgeBeetle()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Inaction Injunction");
    }

    /** Casts Inaction Injunction at the named player2 creature and resolves it. */
    private Permanent detain(String targetName) {
        UUID targetId = harness.getPermanentId(player2, targetName);
        harness.setHand(player1, List.of(new InactionInjunction()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, targetId);
        return gqs.findPermanentById(gd, targetId);
    }

    /** Attempts to declare the given player2 creature as an attacker. */
    private void declareAttack(Permanent creature) {
        creature.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        gs.declareAttackers(gd, player2, List.of(index));
    }
}
