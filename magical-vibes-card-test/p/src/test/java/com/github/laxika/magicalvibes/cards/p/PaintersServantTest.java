package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.c.CeruleanWisps;
import com.github.laxika.magicalvibes.cards.o.OonaQueenOfTheFae;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PaintersServant.class, Forest.class, CeruleanWisps.class, OonaQueenOfTheFae.class, GrizzlyBears.class,
        Humility.class})
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
        Permanent servant = harness.addToBattlefieldAndReturn(player1, new PaintersServant());
        servant.setChosenColor(chosenColor);
        return servant;
    }

    @Test
    @DisplayName("Adds the chosen color to your permanents without removing their own color")
    void addsChosenColorAdditively() {
        harness.addToBattlefield(player1, createCreature("Red Goblin", CardColor.RED));
        addPaintersServant(CardColor.BLUE);

        Permanent goblin = findPermanent(player1, "Red Goblin");

        assertThat(gqs.getEffectiveColors(gd, goblin))
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.BLUE);
    }

    @Test
    @DisplayName("Opponent's permanents also gain the chosen color")
    void addsChosenColorToOpponentPermanents() {
        harness.addToBattlefield(player2, createCreature("Green Bear", CardColor.GREEN));
        addPaintersServant(CardColor.BLUE);

        Permanent bear = findPermanent(player2, "Green Bear");

        assertThat(gqs.getEffectiveColors(gd, bear))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
    }

    @Test
    @DisplayName("Lands also gain the chosen color")
    void colorsLands() {
        harness.addToBattlefield(player1, new Forest());
        addPaintersServant(CardColor.BLUE);

        Permanent forest = findPermanent(player1, "Forest");

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

        Permanent goblin = findPermanent(player1, "Red Goblin");

        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Full flow: cast, choose color, all permanents gain it")
    void fullFlow() {
        harness.addToBattlefield(player1, createCreature("Red Goblin", CardColor.RED));
        harness.castFromHand(player1, new PaintersServant(), "{2}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "WHITE");

        Permanent goblin = findPermanent(player1, "Red Goblin");
        assertThat(gqs.getEffectiveColors(gd, goblin))
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
    }

    @Test
    @DisplayName("Cards in both players' hands, libraries, graveyards and exile are the chosen color")
    void colorsCardsInBothPlayersNonBattlefieldZones() {
        addPaintersServant(CardColor.RED);
        for (var player : List.of(player1, player2)) {
            PaintersServant handCard = new PaintersServant();
            PaintersServant libraryCard = new PaintersServant();
            PaintersServant graveyardCard = new PaintersServant();
            PaintersServant exiledCard = new PaintersServant();
            harness.setHand(player, List.of(handCard));
            harness.setLibrary(player, List.of(libraryCard));
            harness.setGraveyard(player, List.of(graveyardCard));
            harness.setExile(player, List.of(exiledCard));

            for (Card card : List.of(handCard, libraryCard, graveyardCard, exiledCard)) {
                assertThat(gqs.getEffectiveCardColors(gd, card)).containsExactly(CardColor.RED);
            }
        }
    }

    @Test
    @DisplayName("An opponent's spell on the stack is the chosen color in addition to its own")
    void colorsOpponentSpellOnStackAdditively() {
        addPaintersServant(CardColor.RED);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaintersServant());
        CeruleanWisps wisps = new CeruleanWisps();
        harness.setHand(player2, List.of(wisps));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, target.getId());

        assertThat(gqs.getEffectiveCardColors(gd, gd.stack.getLast().getCard()))
                .containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
    }

    @Test
    @DisplayName("Cards Oona exiles count as the chosen color")
    void chosenColorCountsForCardsExiledByOona() {
        harness.addToBattlefield(player1, new OonaQueenOfTheFae());
        addPaintersServant(CardColor.RED);
        harness.setLibrary(player2, List.of(new PaintersServant(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(countPermanents(player1, "Faerie Rogue")).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Painter's Servants add both chosen colors")
    void multipleServantsAddBothChosenColors() {
        Permanent first = addPaintersServant(CardColor.RED);
        Permanent second = addPaintersServant(CardColor.BLUE);

        assertThat(gqs.getEffectiveColors(gd, first))
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.BLUE);
        assertThat(gqs.getEffectiveColors(gd, second))
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.BLUE);
    }

    @Test
    @DisplayName("The color grant ends when Painter's Servant leaves the battlefield")
    void colorGrantEndsWhenServantLeavesBattlefield() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent servant = addPaintersServant(CardColor.RED);
        assertThat(gqs.getEffectiveColors(gd, forest)).containsExactly(CardColor.RED);

        gd.playerBattlefields.get(player1.getId()).remove(servant);
        harness.setGraveyard(player1, List.of(servant.getCard()));

        assertThat(gqs.getEffectiveColors(gd, forest)).isEmpty();
    }

    @Test
    @DisplayName("A later color-setting effect overwrites the chosen color")
    void laterColorSettingEffectOverwritesChosenColor() {
        Permanent servant = addPaintersServant(CardColor.RED);
        harness.setHand(player1, List.of(new CeruleanWisps()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, servant.getId());

        assertThat(gqs.getEffectiveColors(gd, servant)).containsExactly(CardColor.BLUE);
    }
    @Test
    @DisplayName("A Painter's Servant that lost its abilities to a static effect doesn't color cards off the battlefield")
    void lostAbilitiesStopColoringCardsOffTheBattlefield() {
        harness.addToBattlefield(player2, new Humility());
        addPaintersServant(CardColor.RED);
        GrizzlyBears handCard = new GrizzlyBears();
        harness.setHand(player1, List.of(handCard));

        assertThat(gqs.getEffectiveCardColors(gd, handCard)).containsExactly(CardColor.GREEN);
    }
}
