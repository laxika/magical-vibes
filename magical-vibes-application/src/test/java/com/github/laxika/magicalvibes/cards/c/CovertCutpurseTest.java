package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DawnhartMentor;
import com.github.laxika.magicalvibes.cards.f.FlipTheSwitch;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CovertCutpurse.class, CovetousGeist.class, DawnhartMentor.class, FlipTheSwitch.class})
class CovertCutpurseTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys an opponent's creature dealt damage this turn")
    void etbDestroysDamagedOpponentCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new DawnhartMentor()).getId();
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.setHand(player1, List.of(new CovertCutpurse()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dawnhart Mentor");
        harness.assertInGraveyard(player2, "Dawnhart Mentor");
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new DawnhartMentor()).getId();

        harness.setHand(player1, List.of(new CovertCutpurse()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    @DisplayName("Disturb enters transformed as Covetous Geist")
    void disturbEntersTransformed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new CovertCutpurse()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        Permanent geist = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(geist.isTransformed()).isTrue();
        assertThat(geist.getCard()).isInstanceOf(CovetousGeist.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Covetous Geist is exiled instead of going to the graveyard")
    void covetousGeistIsExiledInsteadOfGraveyard() {
        Permanent geist = putTransformedGeistOnBattlefield();
        UUID cardId = geist.getOriginalCard().getId();

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, geist));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(cardId);
    }

    @Test
    @DisplayName("ETB cannot target your own damaged creature")
    void cannotTargetOwnDamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawnhartMentor());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.setHand(player1, List.of(new CovertCutpurse()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Dawnhart Mentor");
    }

    @Test
    @DisplayName("Cutpurse can enter when there is no legal ETB target")
    void entersWithoutLegalTarget() {
        harness.addToBattlefield(player2, new DawnhartMentor());
        harness.castFromHand(player1, new CovertCutpurse(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Covert Cutpurse");
        harness.assertOnBattlefield(player2, "Dawnhart Mentor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Disturb does not trigger the front face's destruction ability")
    void disturbDoesNotDestroyDamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DawnhartMentor());
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        putTransformedGeistOnBattlefield();

        harness.assertOnBattlefield(player2, "Dawnhart Mentor");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Disturb requires its five-mana cost")
    void disturbCannotUseFrontFaceManaCost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new CovertCutpurse()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Covert Cutpurse");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The front face goes to the graveyard normally")
    void frontFaceDoesNotHaveGeistsExileReplacement() {
        Permanent cutpurse = harness.addToBattlefieldAndReturn(player1, new CovertCutpurse());
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, cutpurse));

        harness.assertInGraveyard(player1, "Covert Cutpurse");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Countering a disturbed Geist exiles the physical card")
    void counteredDisturbSpellIsExiled() {
        CovertCutpurse cutpurse = new CovertCutpurse();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(cutpurse));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setHand(player2, List.of(new FlipTheSwitch()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFlashback(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, cutpurse.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .contains(cutpurse.getId());
        harness.assertNotOnBattlefield(player1, "Covetous Geist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A ground creature cannot block the disturbed Geist")
    void geistCannotBeBlockedByGroundCreature() {
        Permanent geist = putTransformedGeistOnBattlefield();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new DawnhartMentor());

        assertThat(bls.canBlockAttacker(gd, blocker, geist,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Geist's deathtouch destroys a creature with more toughness than its power")
    void geistDeathtouchKillsLargerAttacker() {
        Permanent geist = putTransformedGeistOnBattlefield();
        Permanent attacker = addCreatureReady(player2, new DawnhartMentor());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(geist);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dawnhart Mentor");
        harness.assertOnBattlefield(player1, "Covetous Geist");
    }

    private Permanent putTransformedGeistOnBattlefield() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new CovertCutpurse()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
