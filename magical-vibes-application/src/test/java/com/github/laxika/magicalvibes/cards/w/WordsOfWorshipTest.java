package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WordsOfWorship.class, GrizzlyBears.class})
class WordsOfWorshipTest extends BaseCardTest {

    @Test
    @DisplayName("The next draw is replaced by gaining 5 life")
    void replacesNextDrawWithFiveLife() {
        harness.addToBattlefield(player1, new WordsOfWorship());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        prepareAbilityActivation();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        draw(player1);

        harness.assertLife(player1, 25);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Repeated activations replace successive draws")
    void repeatedActivationsReplaceSuccessiveDraws() {
        harness.addToBattlefield(player1, new WordsOfWorship());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);
        prepareAbilityActivation();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        draw(player1);
        draw(player1);

        harness.assertLife(player1, 30);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("After the replacement is used, a later draw is normal")
    void laterDrawIsNormal() {
        harness.addToBattlefield(player1, new WordsOfWorship());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);
        prepareAbilityActivation();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        draw(player1);
        draw(player1);

        harness.assertLife(player1, 25);
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void prepareAbilityActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void draw(com.github.laxika.magicalvibes.model.Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
