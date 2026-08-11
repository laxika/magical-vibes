package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MudbuttonCursetosserTest extends BaseCardTest {

    @Test
    @DisplayName("Requires the additional {2} without a Goblin")
    void requiresAdditionalManaWithoutGoblin() {
        harness.setHand(player1, List.of(new MudbuttonCursetosser()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast for {B} while controlling a Goblin")
    void controlledGoblinWaivesAdditionalMana() {
        harness.addToBattlefield(player1, new GoblinPiker());
        MudbuttonCursetosser card = new MudbuttonCursetosser();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
    }

    @Test
    @DisplayName("Can be cast for {B} with a Goblin card in hand")
    void goblinCardInHandWaivesAdditionalMana() {
        MudbuttonCursetosser card = new MudbuttonCursetosser();
        GoblinPiker goblin = new GoblinPiker();
        harness.setHand(player1, List.of(card, goblin));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(candidate -> candidate.getId().equals(goblin.getId()));
    }

    @Test
    @DisplayName("Cannot block")
    void cannotBlock() {
        Permanent cursetosser = new Permanent(new MudbuttonCursetosser());
        cursetosser.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(cursetosser);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("When it dies, destroys a target opponent creature with power 2 or less")
    void deathTriggerDestroysTargetCreature() {
        harness.addToBattlefield(player1, new MudbuttonCursetosser());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID cursetosserId = harness.getPermanentId(player1, "Mudbutton Cursetosser");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, cursetosserId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bearsId));
    }

    @Test
    @DisplayName("Death trigger only offers opponent creatures with power 2 or less")
    void deathTriggerTargetFilter() {
        harness.addToBattlefield(player1, new MudbuttonCursetosser());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears largeCreature = new GrizzlyBears();
        largeCreature.setPower(3);
        largeCreature.setToughness(3);
        harness.addToBattlefield(player2, largeCreature);

        UUID cursetosserId = harness.getPermanentId(player1, "Mudbutton Cursetosser");
        UUID ownCreatureId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opponentSmallId = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .findFirst().orElseThrow().getId();
        UUID opponentLargeId = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .filter(permanent -> permanent.getCard().getPower() == 3)
                .findFirst().orElseThrow().getId();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, cursetosserId);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        PendingInteraction.PermanentChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(opponentSmallId);
        assertThat(choice.validIds()).doesNotContain(ownCreatureId, opponentLargeId);
    }
}
