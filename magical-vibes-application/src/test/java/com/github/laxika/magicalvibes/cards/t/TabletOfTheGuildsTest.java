package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TabletOfTheGuildsTest extends BaseCardTest {

    private static Card createCreature(String name, List<CardColor> colors) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setColors(colors);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    @Test
    @DisplayName("Choosing two colors as the artifact enters stores both distinct colors")
    void choosesTwoDistinctColorsOnEntry() {
        harness.setHand(player1, List.of(new TabletOfTheGuilds()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.ColorChoice.class))
                .isNotNull();
        harness.handleListChoice(player1, "WHITE");

        PendingInteraction.ColorChoice secondChoice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(secondChoice.options()).doesNotContain("WHITE");

        harness.handleListChoice(player1, "BLUE");

        Permanent tablet = findPermanent(player1, "Tablet of the Guilds");
        assertThat(tablet.getChosenColors()).containsExactly(CardColor.WHITE, CardColor.BLUE);
    }

    @Test
    @DisplayName("A spell matching both chosen colors gains two life")
    void gainsOneLifeForEachChosenColorInSpell() {
        harness.addToBattlefield(player1, new TabletOfTheGuilds());
        Permanent tablet = findPermanent(player1, "Tablet of the Guilds");
        tablet.getChosenColors().addAll(List.of(CardColor.WHITE, CardColor.BLUE));

        harness.setHand(player1, List.of(createCreature("White Blue Creature",
                List.of(CardColor.WHITE, CardColor.BLUE))));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("A spell with none of the chosen colors does not trigger")
    void doesNotTriggerForUnchosenColors() {
        harness.addToBattlefield(player1, new TabletOfTheGuilds());
        Permanent tablet = findPermanent(player1, "Tablet of the Guilds");
        tablet.getChosenColors().addAll(List.of(CardColor.WHITE, CardColor.BLUE));

        harness.setHand(player1, List.of(createCreature("Red Creature", List.of(CardColor.RED))));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}
