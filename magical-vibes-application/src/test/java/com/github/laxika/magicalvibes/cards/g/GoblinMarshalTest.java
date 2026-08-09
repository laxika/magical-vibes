package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinMarshalTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates two 1/1 red Goblin tokens")
    void entersCreatesGoblinTokens() {
        harness.setHand(player1, List.of(new GoblinMarshal()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertGoblinTokens(player1, 2);
    }

    @Test
    @DisplayName("When Goblin Marshal dies, it creates two 1/1 red Goblin tokens")
    void diesCreatesGoblinTokens() {
        harness.addToBattlefield(player1, new GoblinMarshal());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertGoblinTokens(player1, 2);
    }

    @Test
    @DisplayName("Paying echo {4}{R}{R} keeps Goblin Marshal on the battlefield")
    void payingEchoKeepsGoblinMarshal() {
        harness.addToBattlefield(player1, new GoblinMarshal());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 6);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Goblin Marshal");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining echo sacrifices Goblin Marshal")
    void decliningEchoSacrificesGoblinMarshal() {
        harness.addToBattlefield(player1, new GoblinMarshal());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Goblin Marshal");
        harness.assertInGraveyard(player1, "Goblin Marshal");
    }

    private void assertGoblinTokens(com.github.laxika.magicalvibes.model.Player player, int amount) {
        List<Permanent> tokens = gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Goblin"))
                .toList();

        assertThat(tokens).hasSize(amount);
        for (Permanent token : tokens) {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
        }
    }
}
