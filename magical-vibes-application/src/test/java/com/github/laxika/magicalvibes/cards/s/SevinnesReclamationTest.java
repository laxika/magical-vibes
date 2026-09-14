package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SevinnesReclamation.class, GrizzlyBears.class, AirElemental.class})
class SevinnesReclamationTest extends BaseCardTest {

    @Test
    void returnsTargetPermanentWithManaValueThreeOrLessToTheBattlefield() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new SevinnesReclamation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Sevinne's Reclamation");
    }

    @Test
    void rejectsPermanentWithManaValueGreaterThanThree() {
        AirElemental elemental = new AirElemental();
        harness.setGraveyard(player1, List.of(elemental));
        harness.setHand(player1, List.of(new SevinnesReclamation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, elemental.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flashbackMayCopyTheSpellAfterReturningItsTarget() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new SevinnesReclamation(), bears));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        GameData gameData = harness.getGameData();
        StackEntry copy = gameData.stack.stream()
                .filter(StackEntry::isCopy)
                .findFirst()
                .orElseThrow();
        assertThat(copy.getControllerId()).isEqualTo(player1.getId());
        assertThat(copy.getTargetId()).isEqualTo(bears.getId());

        harness.handleMayAbilityChosen(player1, false);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void flashbackMayDeclineTheCopy() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new SevinnesReclamation(), bears));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
