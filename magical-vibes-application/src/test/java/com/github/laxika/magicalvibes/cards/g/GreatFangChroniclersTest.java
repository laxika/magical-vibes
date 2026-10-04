package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MuragandaPetroglyphs;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreatFangChroniclers.class, MuragandaPetroglyphs.class})
class GreatFangChroniclersTest extends BaseCardTest {

    @Test
    @DisplayName("Double team conjures a copy and removes itself from both cards")
    void doubleTeamConjuresCopyAndRemovesItselfFromBothCards() {
        Permanent chroniclers = addReadyChroniclers(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, chroniclers, Keyword.DOUBLE_TEAM)).isFalse();
        Card copy = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Great Fang Chroniclers"))
                .findFirst()
                .orElseThrow();
        assertThat(copy.getKeywords()).doesNotContain(Keyword.DOUBLE_TEAM);
    }

    @Test
    @DisplayName("Double team does not trigger for a token")
    void doubleTeamDoesNotTriggerForToken() {
        GreatFangChroniclers tokenCard = new GreatFangChroniclers();
        tokenCard.setToken(true);
        addCreatureReady(player1, tokenCard);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertNotInHand(player1, "Great Fang Chroniclers");
    }

    @Test
    @DisplayName("The Muraganda Petroglyphs ability conjures the enchantment only once")
    void conjuresMuragandaPetroglyphsOnlyOnce() {
        addReadyChroniclers(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Muraganda Petroglyphs")).hasSize(1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Losing both abilities lets the conjured Petroglyphs boost Chroniclers")
    void petroglyphsBoostsChroniclersAfterBothAbilitiesAreLost() {
        Permanent chroniclers = addReadyChroniclers(player1);
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Muraganda Petroglyphs");
        assertThat(gqs.getEffectivePower(gd, chroniclers)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, chroniclers)).isEqualTo(4);
    }

    @Test
    @DisplayName("The conjure ability cannot be activated during combat")
    void cannotActivateDuringCombat() {
        addReadyChroniclers(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertNotOnBattlefield(player1, "Muraganda Petroglyphs");
    }

    @Test
    @DisplayName("The conjure ability cannot be activated on an opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyChroniclers(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertNotOnBattlefield(player1, "Muraganda Petroglyphs");
    }

    @Test
    @DisplayName("A double-team duplicate retains its own conjure ability")
    void doubleTeamDuplicateCanConjurePetroglyphsIndependently() {
        addReadyChroniclers(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveAllTriggers();
        Card duplicate = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Great Fang Chroniclers"))
                .findFirst().orElseThrow();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, duplicate, "{1}{G}");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Muraganda Petroglyphs")).hasSize(2);
    }

    private Permanent addReadyChroniclers(Player player) {
        return addCreatureReady(player, new GreatFangChroniclers());
    }
}
