package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MuragandaPetroglyphs;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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

        assertThat(gqs.getEffectivePower(gd, chroniclers)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, chroniclers)).isEqualTo(2);
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
        Permanent token = addCreatureReady(player1, tokenCard);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .noneMatch(card -> card.getName().equals("Great Fang Chroniclers"))).isTrue();
    }

    @Test
    @DisplayName("The Muraganda Petroglyphs ability conjures the enchantment only once")
    void conjuresMuragandaPetroglyphsOnlyOnce() {
        Permanent chroniclers = addReadyChroniclers(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Muraganda Petroglyphs")).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    private Permanent addReadyChroniclers(Player player) {
        return addCreatureReady(player, new GreatFangChroniclers());
    }
}
