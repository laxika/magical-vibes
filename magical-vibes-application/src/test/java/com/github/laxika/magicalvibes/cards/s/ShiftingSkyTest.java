package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShiftingSky.class, Forest.class, GrizzlyBears.class, ImprisonedInTheMoon.class, Millstone.class})
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
        Permanent shiftingSky = harness.addToBattlefieldAndReturn(player1, new ShiftingSky());
        shiftingSky.setChosenColor(chosenColor);
        return shiftingSky;
    }

    @Test
    @DisplayName("Your nonland permanents become the chosen color, replacing their colors")
    void recolorsOwnNonlandPermanents() {
        harness.addToBattlefield(player1, createCreature("Red Goblin", CardColor.RED));
        addShiftingSky(CardColor.WHITE);

        Permanent goblin = findPermanent(player1, "Red Goblin");

        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.WHITE);
        assertThat(gqs.getEffectiveColors(gd, goblin)).doesNotContain(CardColor.RED);
    }

    @Test
    @DisplayName("Opponent's nonland permanents also become the chosen color")
    void recolorsOpponentNonlandPermanents() {
        harness.addToBattlefield(player2, createCreature("Green Bear", CardColor.GREEN));
        addShiftingSky(CardColor.WHITE);

        Permanent bear = findPermanent(player2, "Green Bear");

        assertThat(gqs.getEffectiveColors(gd, bear)).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("Lands are not recolored")
    void doesNotRecolorLands() {
        harness.addToBattlefield(player1, new Forest());
        addShiftingSky(CardColor.WHITE);

        Permanent forest = findPermanent(player1, "Forest");

        assertThat(gqs.getEffectiveColors(gd, forest)).doesNotContain(CardColor.WHITE);
    }

    @Test
    @DisplayName("Noncreature artifacts also become the chosen color")
    void recolorsNoncreatureArtifacts() {
        harness.addToBattlefield(player1, new Millstone());
        addShiftingSky(CardColor.WHITE);

        Permanent millstone = findPermanent(player1, "Millstone");

        assertThat(gqs.getEffectiveColors(gd, millstone)).containsExactly(CardColor.WHITE);
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

        Permanent goblin = findPermanent(player1, "Red Goblin");

        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Nonland permanents entering later also become the chosen color")
    void recolorsNonlandPermanentsEnteringLater() {
        addShiftingSky(CardColor.WHITE);
        harness.addToBattlefield(player1, createCreature("Late Goblin", CardColor.RED));

        Permanent goblin = findPermanent(player1, "Late Goblin");

        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("A permanent that becomes a land is not recolored")
    void doesNotRecolorPermanentThatBecomesLand() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent moon = harness.addToBattlefieldAndReturn(player1, new ImprisonedInTheMoon());
        moon.setAttachedTo(bear.getId());
        addShiftingSky(CardColor.WHITE);

        assertThat(gqs.isLand(gd, bear)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, bear)).isEmpty();
    }

    @Test
    @DisplayName("Full flow: cast, resolve, choose color, all nonland permanents become it")
    void fullFlow() {
        harness.addToBattlefield(player1, createCreature("Red Goblin", CardColor.RED));
        harness.castFromHand(player1, new ShiftingSky(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "WHITE");

        Permanent goblin = findPermanent(player1, "Red Goblin");
        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.WHITE);
    }
}
