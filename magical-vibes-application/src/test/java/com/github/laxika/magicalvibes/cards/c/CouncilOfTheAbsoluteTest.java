package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouncilOfTheAbsoluteTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Council of the Absolute awaits a card name choice and records it")
    void resolvingChoosesCardName() {
        harness.setHand(player1, List.of(new CouncilOfTheAbsolute()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Divination");

        Permanent council = findPermanent(player1, "Council of the Absolute");
        assertThat(council.getChosenName()).isEqualTo("Divination");
    }

    @Test
    @DisplayName("Opponents can't cast spells with the chosen name")
    void opponentCannotCastChosenName() {
        addReadyCouncil(player1, "Divination");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opponents can still cast spells with a different name")
    void opponentCanCastOtherNames() {
        addReadyCouncil(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player2, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The controller casts a spell with the chosen name for {2} less")
    void controllerGetsCostReduction() {
        addReadyCouncil(player1, "Divination");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A spell with a different name gets no discount")
    void otherSpellsAreNotDiscounted() {
        addReadyCouncil(player1, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private Permanent addReadyCouncil(Player player, String chosenName) {
        Permanent perm = new Permanent(new CouncilOfTheAbsolute());
        perm.setSummoningSick(false);
        perm.setChosenName(chosenName);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
