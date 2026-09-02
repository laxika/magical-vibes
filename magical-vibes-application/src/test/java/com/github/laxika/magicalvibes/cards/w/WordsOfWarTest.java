package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({WordsOfWar.class, GrizzlyBears.class})
class WordsOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("The next draw is replaced by 2 damage to the chosen player")
    void replacesNextDrawWithDamageToPlayer() {
        harness.addToBattlefield(player1, new WordsOfWar());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);

        activateWordsOfWar(player2.getId());
        draw(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The replacement can deal damage to a chosen permanent")
    void replacesNextDrawWithDamageToPermanent() {
        harness.addToBattlefield(player1, new WordsOfWar());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        activateWordsOfWar(target.getId());
        draw(player1);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Repeated activations replace successive draws")
    void repeatedActivationsReplaceSuccessiveDraws() {
        harness.addToBattlefield(player1, new WordsOfWar());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player2, 20);

        activateWordsOfWar(player2.getId());
        activateWordsOfWar(player2.getId());

        draw(player1);
        draw(player1);
        draw(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    private void activateWordsOfWar(java.util.UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
