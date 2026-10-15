package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MilitiasPride;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarmonizedCrescendo.class, AvianChangeling.class, GrizzlyBears.class,
        Shock.class, MilitiasPride.class})
class HarmonizedCrescendoTest extends BaseCardTest {

    private void payAndCast(Player player) {
        harness.addMana(player, ManaColor.BLUE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 4);
        harness.setHand(player, List.of(new HarmonizedCrescendo()));
        harness.castAndResolveInstant(player, 0);
    }

    private void stockLibrary(Player player, int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new Shock());
        }
        harness.setLibrary(player, deck);
    }

    @Test
    @DisplayName("Draws a card for each permanent of the chosen type you control")
    void drawsPerChosenTypeCount() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        stockLibrary(player1, 5);

        payAndCast(player1);
        harness.handleListChoice(player1, "BEAR");

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Choosing a type you control none of draws no cards")
    void chosenTypeYouControlNoneDrawsZero() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        stockLibrary(player1, 5);

        payAndCast(player1);
        harness.handleListChoice(player1, "GOBLIN");

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("A Changeling you control counts as the chosen type")
    void changelingCountsAsChosenType() {
        harness.addToBattlefield(player1, new AvianChangeling());
        stockLibrary(player1, 5);

        payAndCast(player1);
        harness.handleListChoice(player1, "GOBLIN");

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Only the caster's permanents of the chosen type are counted")
    void onlyControllerPermanentsCounted() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        stockLibrary(player1, 5);

        payAndCast(player1);
        harness.handleListChoice(player1, "BEAR");

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Noncreature kindred permanents count alongside creatures of the chosen type")
    void countsKindredEnchantmentsAndChangelingsOnceEach() {
        harness.addToBattlefield(player1, new MilitiasPride());
        harness.addToBattlefield(player1, new AvianChangeling());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new MilitiasPride());
        stockLibrary(player1, 5);

        payAndCast(player1);
        harness.handleListChoice(player1, "KITHKIN");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Summoning-sick creatures can convoke and still count while tapped")
    void convokedCreaturesStillCount() {
        List<Permanent> bears = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            bear.setSummoningSick(true);
            bears.add(bear);
        }
        stockLibrary(player1, 5);
        harness.setHand(player1, List.of(new HarmonizedCrescendo()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(),
                bears.stream().map(Permanent::getId).toList());

        assertThat(bears).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Counts permanents at resolution after an opponent removes one in response")
    void countsPermanentsAtResolution() {
        Permanent removedBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        stockLibrary(player1, 5);
        harness.setHand(player1, List.of(new HarmonizedCrescendo()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.castAndResolveInstant(player2, 0, removedBear.getId());
        resolveAllTriggers();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(removedBear);
    }

    @Test
    @DisplayName("A later Crescendo asks for a new creature type")
    void consecutiveSpellsChooseTypesIndependently() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        stockLibrary(player1, 5);

        payAndCast(player1);
        harness.handleListChoice(player1, "BEAR");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        Card previouslyDrawn = gd.playerHands.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new HarmonizedCrescendo(), previouslyDrawn));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player1, 0);
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(previouslyDrawn);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }
}
