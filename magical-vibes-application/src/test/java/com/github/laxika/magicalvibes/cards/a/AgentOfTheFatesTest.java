package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AgentOfTheFatesTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Agent of the Fates makes each opponent sacrifice a creature")
    void castingSpellThatTargetsAgentMakesOpponentSacrificeCreature() {
        harness.addToBattlefield(player1, new AgentOfTheFates());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID agentId = harness.getPermanentId(player1, "Agent of the Fates");
        harness.castInstant(player1, 0, agentId);
        harness.passBothPriorities();

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

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

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
        harness.castInstant(player2, 0, agentId);
        harness.passBothPriorities();

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
        harness.castInstant(player1, 0, agentId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);
        harness.handlePermanentChosen(player2, spiderId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }
}
