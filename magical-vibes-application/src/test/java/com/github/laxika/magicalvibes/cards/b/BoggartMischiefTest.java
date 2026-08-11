package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoggRaider;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void castBoggartMischief() {
        harness.setHand(player1, List.of(new BoggartMischief()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void destroyAllCreatures() {
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
