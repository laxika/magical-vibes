package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WordsOfWilding.class, Forest.class})
class WordsOfWildingTest extends BaseCardTest {

    @Test
    @DisplayName("The next draw is replaced by creating a Bear token")
    void replacesNextDrawWithBearToken() {
        harness.addToBattlefield(player1, new WordsOfWilding());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        activateWordsOfWilding(1);
        draw(player1);

        Permanent bear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Bear"))
                .findFirst()
                .orElseThrow();
        assertThat(bear.getCard().getPower()).isEqualTo(2);
        assertThat(bear.getCard().getToughness()).isEqualTo(2);
        assertThat(bear.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(bear.getCard().getSubtypes()).containsExactly(CardSubtype.BEAR);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Repeated activations replace successive draws")
    void repeatedActivationsReplaceSuccessiveDraws() {
        harness.addToBattlefield(player1, new WordsOfWilding());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        activateWordsOfWilding(2);
        draw(player1);
        draw(player1);

        assertThat(countPermanents(player1, "Bear")).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("After the replacement is used, a later draw is normal")
    void laterDrawIsNormal() {
        harness.addToBattlefield(player1, new WordsOfWilding());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        activateWordsOfWilding(1);
        draw(player1);
        draw(player1);

        assertThat(countPermanents(player1, "Bear")).isEqualTo(1);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The delayed replacement expires at cleanup")
    void replacementExpiresAtCleanup() {
        harness.addToBattlefield(player1, new WordsOfWilding());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        activateWordsOfWilding(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        draw(player1);

        assertThat(countPermanents(player1, "Bear")).isZero();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("The replacement applies only to its controller's draw")
    void replacementAppliesOnlyToControllerDraw() {
        harness.addToBattlefield(player1, new WordsOfWilding());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        activateWordsOfWilding(1);
        draw(player2);

        assertThat(countPermanents(player1, "Bear")).isZero();
        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        draw(player1);

        assertThat(countPermanents(player1, "Bear")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The replacement creates a Bear even when the library is empty")
    void replacementWorksWithEmptyLibrary() {
        harness.addToBattlefield(player1, new WordsOfWilding());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        activateWordsOfWilding(1);
        draw(player1);

        assertThat(countPermanents(player1, "Bear")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void activateWordsOfWilding(int activations) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, activations);
        for (int i = 0; i < activations; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
