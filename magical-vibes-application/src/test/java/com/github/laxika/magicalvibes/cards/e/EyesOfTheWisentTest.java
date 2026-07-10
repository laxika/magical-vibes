package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EyesOfTheWisentTest extends BaseCardTest {

    /** Player1 controls Eyes of the Wisent and a bear; it is player1's turn. */
    private UUID setUpControllerTurn() {
        harness.addToBattlefield(player1, new EyesOfTheWisent());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return harness.getPermanentId(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent's blue spell on your turn: accepting creates a 4/4 green Elemental")
    void opponentBlueSpellCreatesToken() {
        UUID bearsId = setUpControllerTurn();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, bearsId);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the token-creation trigger

        Permanent token = findPermanent(player1, "Elemental");
        assertThat(token).isNotNull();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining the may ability creates no token")
    void decliningCreatesNoToken() {
        UUID bearsId = setUpControllerTurn();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, bearsId);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elemental"));
    }

    @Test
    @DisplayName("Opponent's non-blue spell on your turn does not trigger")
    void nonBlueSpellDoesNotTrigger() {
        UUID bearsId = setUpControllerTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bearsId);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elemental"));
    }

    @Test
    @DisplayName("Opponent's blue spell during the opponent's own turn does not trigger")
    void blueSpellOnOpponentTurnDoesNotTrigger() {
        harness.addToBattlefield(player1, new EyesOfTheWisent());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, bearsId);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elemental"));
    }

    @Test
    @DisplayName("Your own blue spell does not trigger (only opponents' casts count)")
    void ownBlueSpellDoesNotTrigger() {
        UUID bearsId = setUpControllerTurn();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bearsId);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elemental"));
    }
}
