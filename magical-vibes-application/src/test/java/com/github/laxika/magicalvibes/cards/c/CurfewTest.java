package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Curfew")
class CurfewTest extends BaseCardTest {

    @Test
    @DisplayName("Each player chooses one creature before the chosen creatures return")
    void eachPlayerChoosesBeforeReturns() {
        GameData gd = harness.getGameData();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GiantSpider());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        Permanent player1Bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent player1Spider = gd.playerBattlefields.get(player1.getId()).get(1);
        Permanent player2Bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        Permanent player2Spider = gd.playerBattlefields.get(player2.getId()).get(1);

        castCurfew();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validIds()).containsExactly(player1Bears.getId(), player1Spider.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(player1Bears.getId()));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.validIds()).containsExactly(player2Bears.getId(), player2Spider.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(player1Bears, player1Spider);

        harness.handleMultiplePermanentsChosen(player2, List.of(player2Bears.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(player1Spider);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(player2Spider);
        assertThat(gd.playerHands.get(player1.getId())).contains(player1Bears.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Bears.getCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Returns the only creature each player controls without prompting")
    void returnsOnlyCreatureWithoutPrompt() {
        GameData gd = harness.getGameData();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        Permanent player1Creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent player2Creature = gd.playerBattlefields.get(player2.getId()).getFirst();

        castCurfew();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(player1Creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(player2Creature);
        assertThat(gd.playerHands.get(player1.getId())).contains(player1Creature.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Creature.getCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castCurfew() {
        harness.setHand(player1, List.of(new Curfew()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
    }
}
