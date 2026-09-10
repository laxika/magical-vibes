package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.k.KnightOfDawn;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.l.LightningElemental;
import com.github.laxika.magicalvibes.cards.s.SeleniaDarkAngel;
import com.github.laxika.magicalvibes.cards.t.TheWaterCrystal;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Grindstone.class, BottleGnomes.class, KnightOfDawn.class, LightningElemental.class,
        SeleniaDarkAngel.class, TrainedArmodon.class})
class GrindstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Repeats while each milled pair shares a color, stopping on the first mismatched pair")
    void repeatsWhilePairsShareAColor() {
        harness.addToBattlefield(player1, new Grindstone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Card survivor = new LightningElemental();
        harness.setLibrary(player2, List.of(
                new LightningElemental(), new LightningElemental(), new TrainedArmodon(),
                new BottleGnomes(), survivor));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(survivor);
    }

    @Test
    @DisplayName("A pair with no shared color mills only two cards")
    void mismatchedFirstPairStopsImmediately() {
        harness.addToBattlefield(player1, new Grindstone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setLibrary(player2, List.of(
                new LightningElemental(), new TrainedArmodon(), new LightningElemental(),
                new LightningElemental()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Two colorless cards do not share a color, so the process stops")
    void colorlessPairDoesNotRepeat() {
        harness.addToBattlefield(player1, new Grindstone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setLibrary(player2, List.of(
                new BottleGnomes(), new BottleGnomes(), new LightningElemental(),
                new LightningElemental()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A multicolored card shares any one color with its pair")
    void multicoloredCardSharesAnyColor() {
        harness.addToBattlefield(player1, new Grindstone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setLibrary(player2, List.of(
                new SeleniaDarkAngel(), new KnightOfDawn(), new TrainedArmodon(),
                new LightningElemental()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A one-card library mills that card and ends the process")
    void singleCardLibraryEndsProcess() {
        harness.addToBattlefield(player1, new Grindstone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setLibrary(player2, List.of(new LightningElemental()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetYourself() {
        harness.addToBattlefield(player1, new Grindstone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(
                new LightningElemental(), new LightningElemental(), new TrainedArmodon()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty library ends the process without milling")
    void emptyLibraryEndsProcess() {
        harness.addToBattlefield(player1, new Grindstone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @CardUsed(TheWaterCrystal.class)
    @DisplayName("A mill bonus still allows repetition when any two cards in the larger batch share a color")
    void repeatsAfterMillBonusAddsCards() {
        harness.addToBattlefield(player1, new Grindstone());
        harness.addToBattlefield(player1, new TheWaterCrystal());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player2, List.of(
                new LightningElemental(), new LightningElemental(), new TrainedArmodon(),
                new BottleGnomes(), new BottleGnomes(), new BottleGnomes(),
                new BottleGnomes(), new BottleGnomes(), new BottleGnomes(),
                new BottleGnomes(), new BottleGnomes(), new BottleGnomes()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(12);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @CardUsed(LeylineOfTheVoid.class)
    @DisplayName("Cards exiled by a replacement effect still count toward repetition")
    void repeatsWhenMilledCardsAreExiled() {
        harness.addToBattlefield(player1, new Grindstone());
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player2, List.of(
                new LightningElemental(), new LightningElemental(), new BottleGnomes(),
                new BottleGnomes()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
