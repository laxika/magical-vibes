package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CliffhavenSellSword;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlacialGrasp.class, CliffhavenSellSword.class, Forest.class})
class GlacialGraspTest extends BaseCardTest {

    @Test
    @DisplayName("Taps the target, mills its controller, skips its next untap, and draws a card")
    void resolvesAllEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        harness.setLibrary(player1, List.of(new CliffhavenSellSword()));
        harness.setLibrary(player2, List.of(new CliffhavenSellSword(), new Forest(), new CliffhavenSellSword()));
        harness.setHand(player1, List.of(new GlacialGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int opponentLibrarySize = gd.playerDecks.get(player2.getId()).size();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibrarySize - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertInHand(player1, "Cliffhaven Sell-Sword");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new GlacialGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The restriction lasts through only the target controller's next untap step")
    void skipsOnlyNextControllerUntap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new GlacialGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already tapped own creature still mills its controller and allows the draw")
    void tappedOwnCreatureMillsBeforeDrawing() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        target.tap();
        Forest milled = new Forest();
        CliffhavenSellSword drawn = new CliffhavenSellSword();
        harness.setLibrary(player1, List.of(milled, new Forest(), drawn));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new GlacialGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milled);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Cliffhaven Sell-Sword");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mills all available cards from a library with fewer than two cards")
    void millsShortLibraryAndStillDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        Forest milled = new Forest();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(milled));
        harness.setHand(player1, List.of(new GlacialGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(milled);
        harness.assertInHand(player1, "Forest");
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not mill or draw when its only target leaves before resolution")
    void illegalTargetPreventsAllEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new GlacialGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Glacial Grasp");
    }
}
