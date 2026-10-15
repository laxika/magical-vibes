package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonBroodmother.class, GrizzlyBears.class, DoublingSeason.class})
class DragonBroodmotherTest extends BaseCardTest {

    private Permanent dragonToken(Player owner) {
        return findPermanent(owner, "Dragon");
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

        assertThat(dragonToken(player1).getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(findPermanents(player2, "Dragon")).isEmpty();
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

    @Test
    @DisplayName("Devour may sacrifice Dragon Broodmother itself")
    void tokenCanDevourBroodmother() {
        Permanent mother = harness.addToBattlefieldAndReturn(player1, new DragonBroodmother());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(mother.getId()));

        Permanent dragon = dragonToken(player1);
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Dragon Broodmother");
        harness.assertInGraveyard(player1, "Dragon Broodmother");
    }

    @Test
    @DisplayName("Devour 2 counts every sacrificed creature and leaves unchosen creatures alone")
    void devoursMultipleCreatures() {
        harness.addToBattlefield(player1, new DragonBroodmother());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        Permanent dragon = dragonToken(player1);
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(first.getCard(), second.getCard());
        harness.assertOnBattlefield(player1, "Dragon Broodmother");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
    }

    @Test
    @DisplayName("Doubled Dragon tokens each choose devour without sacrificing simultaneously entering tokens")
    void doubledTokensCannotDevourEachOther() {
        harness.addToBattlefield(player1, new DragonBroodmother());
        harness.addToBattlefield(player1, new DoublingSeason());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        int choices = 0;
        while (gd.interaction.isAwaitingInput() && choices < 3) {
            PendingInteraction.MultiPermanentChoice choice =
                    (PendingInteraction.MultiPermanentChoice) gd.interaction.activeInteraction();
            List<Permanent> dragons = findPermanents(player1, "Dragon");
            for (Permanent dragon : dragons) {
                assertThat(choice.validIds()).doesNotContain(dragon.getId());
            }
            declineDevour(player1);
            choices++;
        }

        assertThat(choices).isEqualTo(2);
        assertThat(findPermanents(player1, "Dragon")).hasSize(2)
                .allSatisfy(dragon -> assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }
}
