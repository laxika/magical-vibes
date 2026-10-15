package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EternalStudent.class})
class EternalStudentTest extends BaseCardTest {

    @Test
    @DisplayName("Graveyard ability exiles Eternal Student and creates two flying Inkling tokens")
    void graveyardAbilityExilesSourceAndCreatesTokens() {
        EternalStudent eternalStudent = new EternalStudent();
        harness.setGraveyard(player1, List.of(eternalStudent));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(eternalStudent);

        harness.passBothPriorities();

        List<Permanent> inklings = findPermanents(player1, "Inkling");
        assertThat(inklings).hasSize(2);
        for (Permanent inkling : inklings) {
            assertThat(inkling.getCard().isToken()).isTrue();
            assertThat(gqs.getEffectivePower(gd, inkling)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, inkling)).isEqualTo(1);
            assertThat(inkling.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
            assertThat(inkling.getCard().getSubtypes()).contains(CardSubtype.INKLING);
            assertThat(gqs.hasKeyword(gd, inkling, Keyword.FLYING)).isTrue();
        }
    }

    @Test
    @DisplayName("Graveyard activation requires black mana and leaves the source untouched on failure")
    void cannotActivateWithoutBlackMana() {
        EternalStudent eternalStudent = new EternalStudent();
        harness.setGraveyard(player1, List.of(eternalStudent));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(eternalStudent);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Graveyard activation requires two mana in total")
    void cannotActivateWithOnlyOneBlackMana() {
        EternalStudent eternalStudent = new EternalStudent();
        harness.setGraveyard(player1, List.of(eternalStudent));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(eternalStudent);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Graveyard ability can be activated during the opponent's turn with mixed mana")
    void canActivateDuringOpponentsTurn() {
        harness.setGraveyard(player1, List.of(new EternalStudent()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Inkling")).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
