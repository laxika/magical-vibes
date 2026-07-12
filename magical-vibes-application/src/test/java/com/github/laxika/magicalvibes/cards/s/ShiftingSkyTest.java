package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShiftingSkyTest extends BaseCardTest {

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

    private Permanent addShiftingSky(CardColor chosenColor) {
        Permanent shiftingSky = new Permanent(new ShiftingSky());
        shiftingSky.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player1.getId()).add(shiftingSky);
        return shiftingSky;
    }

    private Permanent permanentNamed(java.util.UUID playerId, String name) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Your nonland permanents become the chosen color, replacing their colors")
    void recolorsOwnNonlandPermanents() {
        harness.addToBattlefield(player1, createCreature("Red Goblin", CardColor.RED));
        addShiftingSky(CardColor.WHITE);

        Permanent goblin = permanentNamed(player1.getId(), "Red Goblin");

        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.WHITE);
        assertThat(gqs.getEffectiveColors(gd, goblin)).doesNotContain(CardColor.RED);
    }

    @Test
    @DisplayName("Opponent's nonland permanents also become the chosen color")
    void recolorsOpponentNonlandPermanents() {
        harness.addToBattlefield(player2, createCreature("Green Bear", CardColor.GREEN));
        addShiftingSky(CardColor.WHITE);

        Permanent bear = permanentNamed(player2.getId(), "Green Bear");

        assertThat(gqs.getEffectiveColors(gd, bear)).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("Lands are not recolored")
    void doesNotRecolorLands() {
        harness.addToBattlefield(player1, new Forest());
        addShiftingSky(CardColor.WHITE);

        Permanent forest = permanentNamed(player1.getId(), "Forest");

        assertThat(gqs.getEffectiveColors(gd, forest)).doesNotContain(CardColor.WHITE);
    }

    @Test
    @DisplayName("Shifting Sky itself becomes the chosen color")
    void recolorsItself() {
        Permanent shiftingSky = addShiftingSky(CardColor.WHITE);

        assertThat(gqs.getEffectiveColors(gd, shiftingSky)).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("No recolor before a color is chosen")
    void noRecolorWithoutChosenColor() {
        harness.addToBattlefield(player1, createCreature("Red Goblin", CardColor.RED));
        harness.addToBattlefield(player1, new ShiftingSky());

        Permanent goblin = permanentNamed(player1.getId(), "Red Goblin");

        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Full flow: cast, resolve, choose color, all nonland permanents become it")
    void fullFlow() {
        harness.addToBattlefield(player1, createCreature("Red Goblin", CardColor.RED));
        harness.setHand(player1, List.of(new ShiftingSky()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "WHITE");

        Permanent goblin = permanentNamed(player1.getId(), "Red Goblin");
        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.WHITE);
    }
}
