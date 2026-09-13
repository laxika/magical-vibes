package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.PlatedSpider;
import com.github.laxika.magicalvibes.cards.r.RecklessAbandon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({GoblinMarshal.class, PlatedSpider.class, RecklessAbandon.class})
class GoblinMarshalTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates two 1/1 red Goblin tokens")
    void entersCreatesGoblinTokens() {
        castAndResolveGoblinMarshal();

        assertGoblinTokens(player1, 2);
    }

    @Test
    @DisplayName("When Goblin Marshal dies, it creates two 1/1 red Goblin tokens")
    void diesCreatesGoblinTokens() {
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new GoblinMarshal());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new PlatedSpider());

        harness.setHand(player1, List.of(new RecklessAbandon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorceryWithSacrifice(player1, 0, marshal.getId(), sacrificed.getId());
        resolveAllTriggers();

        assertGoblinTokens(player1, 2);
    }

    @Test
    @DisplayName("Paying echo {4}{R}{R} keeps Goblin Marshal on the battlefield")
    void payingEchoKeepsGoblinMarshal() {
        castAndResolveGoblinMarshal();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 6);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Goblin Marshal");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Goblin Marshal");
    }

    @Test
    @DisplayName("Declining echo sacrifices Goblin Marshal")
    void decliningEchoSacrificesGoblinMarshal() {
        castAndResolveGoblinMarshal();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Goblin Marshal");
        harness.assertInGraveyard(player1, "Goblin Marshal");
        assertGoblinTokens(player1, 2);
    }

    @Test
    @DisplayName("Echo does not trigger during an opponent's upkeep")
    void echoDoesNotTriggerDuringOpponentsUpkeep() {
        castAndResolveGoblinMarshal();

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Goblin Marshal");
    }

    private void castAndResolveGoblinMarshal() {
        harness.castFromHand(player1, new GoblinMarshal(), "{4}{R}{R}");
        resolveAllTriggers();
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
