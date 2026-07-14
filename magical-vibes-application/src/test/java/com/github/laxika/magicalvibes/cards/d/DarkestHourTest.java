package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DarkestHourTest extends BaseCardTest {

    private static Card createCreature(String name, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Permanent permanentNamed(java.util.UUID playerId, String name) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Your creatures become black, replacing their colors")
    void recolorsOwnCreatures() {
        harness.addToBattlefield(player1, createCreature("Red Goblin", CardColor.RED));
        harness.addToBattlefield(player1, new DarkestHour());

        Permanent goblin = permanentNamed(player1.getId(), "Red Goblin");

        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.BLACK);
        assertThat(gqs.getEffectiveColors(gd, goblin)).doesNotContain(CardColor.RED);
    }

    @Test
    @DisplayName("Opponent's creatures also become black")
    void recolorsOpponentCreatures() {
        harness.addToBattlefield(player2, createCreature("Green Bear", CardColor.GREEN));
        harness.addToBattlefield(player1, new DarkestHour());

        Permanent bear = permanentNamed(player2.getId(), "Green Bear");

        assertThat(gqs.getEffectiveColors(gd, bear)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("Noncreature permanents are not recolored")
    void doesNotRecolorNoncreatures() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new DarkestHour());

        Permanent forest = permanentNamed(player1.getId(), "Forest");

        assertThat(gqs.getEffectiveColors(gd, forest)).doesNotContain(CardColor.BLACK);
    }

    @Test
    @DisplayName("Full flow: cast and resolve, then all creatures are black")
    void fullFlow() {
        harness.addToBattlefield(player1, createCreature("Red Goblin", CardColor.RED));
        harness.setHand(player1, List.of(new DarkestHour()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        Permanent goblin = permanentNamed(player1.getId(), "Red Goblin");
        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.BLACK);
    }
}
