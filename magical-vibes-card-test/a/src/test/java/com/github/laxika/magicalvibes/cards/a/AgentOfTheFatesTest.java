package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgentOfTheFates.class, GiantGrowth.class, GiantSpider.class, GrizzlyBears.class, Shock.class})
class AgentOfTheFatesTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Agent of the Fates makes each opponent sacrifice a creature")
    void castingSpellThatTargetsAgentMakesOpponentSacrificeCreature() {
        harness.addToBattlefield(player1, new AgentOfTheFates());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID agentId = harness.getPermanentId(player1, "Agent of the Fates");
        harness.castAndResolveInstant(player1, 0, agentId);

        harness.assertOnBattlefield(player1, "Agent of the Fates");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Agent of the Fates")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new AgentOfTheFates());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent's spell that targets Agent of the Fates does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new AgentOfTheFates());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID agentId = harness.getPermanentId(player1, "Agent of the Fates");
        harness.castAndResolveInstant(player2, 0, agentId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent chooses which creature to sacrifice")
    void opponentChoosesCreatureToSacrifice() {
        harness.addToBattlefield(player1, new AgentOfTheFates());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID agentId = harness.getPermanentId(player1, "Agent of the Fates");
        UUID spiderId = harness.getPermanentId(player2, "Giant Spider");
        harness.castAndResolveInstant(player1, 0, agentId);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(spiderId));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Targeting a different creature does not trigger heroic")
    void targetingAnotherCreatureDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new AgentOfTheFates());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertOnBattlefield(player2, "Giant Spider");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent with no creatures sacrifices nothing and the targeting spell still resolves")
    void opponentWithNoCreaturesDoesNotPreventSpellResolving() {
        harness.addToBattlefield(player1, new AgentOfTheFates());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Agent of the Fates"));
        harness.assertOnBattlefield(player1, "Agent of the Fates");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Agent of the Fates");
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Heroic still resolves after Agent of the Fates is killed in response")
    void heroicResolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new AgentOfTheFates());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        UUID agentId = harness.getPermanentId(player1, "Agent of the Fates");

        harness.castInstant(player1, 0, agentId);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, agentId);

        harness.assertInGraveyard(player1, "Agent of the Fates");
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Giant Growth");
        assertThat(gd.stack).isEmpty();
    }
}
