package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoggRaider;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoggartMischief.class, GrizzlyBears.class, MoggRaider.class, WrathOfGod.class, Blossombind.class})
class BoggartMischiefTest extends BaseCardTest {

    @Test
    void acceptingEnterTriggerBlightsCreatureAndCreatesGoblinTokens() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBoggartMischief();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        List<Permanent> goblins = findPermanents(player1, "Goblin");
        assertThat(goblins).hasSize(2);
        assertThat(goblins).allSatisfy(goblin -> {
            assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(goblin.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
            assertThat(goblin.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
            assertThat(goblin.getEffectivePower()).isEqualTo(1);
            assertThat(goblin.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    void decliningEnterTriggerDoesNotBlightOrCreateTokens() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBoggartMischief();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    @Test
    void goblinDeathDrainsOpponentAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new BoggartMischief());
        harness.addToBattlefield(player1, new MoggRaider());

        destroyAllCreatures();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void nonGoblinDeathDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new BoggartMischief());
        harness.addToBattlefield(player1, new GrizzlyBears());

        destroyAllCreatures();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void tokensAreCreatedDuringTheEnterAbilityResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBoggartMischief();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Goblin")).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotBlightWhenTheOnlyCreatureCannotHaveCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Blossombind());
        aura.setAttachedTo(creature.getId());
        harness.castFromHand(player1, new BoggartMischief(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        }
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsGoblinDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new BoggartMischief());
        harness.addToBattlefield(player2, new MoggRaider());

        destroyAllCreatures();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void eachGoblinDyingSimultaneouslyTriggersSeparately() {
        harness.addToBattlefield(player1, new BoggartMischief());
        harness.addToBattlefield(player1, new MoggRaider());
        harness.addToBattlefield(player1, new MoggRaider());

        destroyAllCreatures();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void blightingAGoblinToDeathStillCreatesTokensAndTriggersDrain() {
        harness.addToBattlefield(player1, new MoggRaider());
        castBoggartMischief();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mogg Raider")).isEmpty();
        assertThat(findPermanents(player1, "Goblin")).hasSize(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    private void castBoggartMischief() {
        harness.castFromHand(player1, new BoggartMischief(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void destroyAllCreatures() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
