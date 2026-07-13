package com.github.laxika.magicalvibes.cards.p;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaintersServantTest extends BaseCardTest {

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

    private Permanent addPaintersServant(CardColor chosenColor) {
        Permanent servant = new Permanent(new PaintersServant());
        servant.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player1.getId()).add(servant);
        return servant;
    }

    private Permanent permanentNamed(UUID playerId, String name) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Adds the chosen color to your permanents without removing their own color")
    void addsChosenColorAdditively() {
        harness.addToBattlefield(player1, createCreature("Red Goblin", CardColor.RED));
        addPaintersServant(CardColor.BLUE);

        Permanent goblin = permanentNamed(player1.getId(), "Red Goblin");

        assertThat(gqs.getEffectiveColors(gd, goblin))
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.BLUE);
    }

    @Test
    @DisplayName("Opponent's permanents also gain the chosen color")
    void addsChosenColorToOpponentPermanents() {
        harness.addToBattlefield(player2, createCreature("Green Bear", CardColor.GREEN));
        addPaintersServant(CardColor.BLUE);

        Permanent bear = permanentNamed(player2.getId(), "Green Bear");

        assertThat(gqs.getEffectiveColors(gd, bear))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
    }

    @Test
    @DisplayName("Lands also gain the chosen color")
    void colorsLands() {
        harness.addToBattlefield(player1, new Forest());
        addPaintersServant(CardColor.BLUE);

        Permanent forest = permanentNamed(player1.getId(), "Forest");

        assertThat(gqs.getEffectiveColors(gd, forest)).contains(CardColor.BLUE);
    }

    @Test
    @DisplayName("Painter's Servant itself gains the chosen color")
    void colorsItself() {
        Permanent servant = addPaintersServant(CardColor.BLUE);

        assertThat(gqs.getEffectiveColors(gd, servant)).contains(CardColor.BLUE);
    }

    @Test
    @DisplayName("No color change before a color is chosen")
    void noChangeWithoutChosenColor() {
        harness.addToBattlefield(player1, createCreature("Red Goblin", CardColor.RED));
        harness.addToBattlefield(player1, new PaintersServant());

        Permanent goblin = permanentNamed(player1.getId(), "Red Goblin");

        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Full flow: cast, choose color, all permanents gain it")
    void fullFlow() {
        harness.addToBattlefield(player1, createCreature("Red Goblin", CardColor.RED));
        harness.setHand(player1, List.of(new PaintersServant()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "WHITE");

        Permanent goblin = permanentNamed(player1.getId(), "Red Goblin");
        assertThat(gqs.getEffectiveColors(gd, goblin))
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
    }
}
