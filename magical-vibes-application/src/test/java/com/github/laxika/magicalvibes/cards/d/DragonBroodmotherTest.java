package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DragonBroodmotherTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private Permanent dragonToken(Player owner) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();
    }

    /** Devour prompts a multi-permanent choice; decline it (sacrifice nothing). */
    private void declineDevour(Player controller) {
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(controller, List.of());
    }

    @Test
    @DisplayName("Creates a 1/1 red and green Dragon token with flying during controller's upkeep")
    void createsDragonTokenDuringControllersUpkeep() {
        harness.addToBattlefield(player1, new DragonBroodmother());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> token enters with devour
        declineDevour(player1);

        Permanent dragon = dragonToken(player1);
        assertThat(dragon.getCard().getPower()).isEqualTo(1);
        assertThat(dragon.getCard().getToughness()).isEqualTo(1);
        assertThat(dragon.getCard().getColors()).contains(CardColor.RED, CardColor.GREEN);
        assertThat(dragon.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(dragon.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Creates the Dragon token under the controller during an opponent's upkeep")
    void createsTokenDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new DragonBroodmother());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger -> token enters with devour
        declineDevour(player1);

        List<Permanent> p2Tokens = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(dragonToken(player1).getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(p2Tokens).isEmpty();
    }

    @Test
    @DisplayName("Devour 2: sacrificing one creature enters the token with two +1/+1 counters")
    void devourDoublesCountersFromSacrifice() {
        harness.addToBattlefield(player1, new DragonBroodmother());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> token enters with devour

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));

        Permanent dragon = dragonToken(player1);
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodder);
    }
}
