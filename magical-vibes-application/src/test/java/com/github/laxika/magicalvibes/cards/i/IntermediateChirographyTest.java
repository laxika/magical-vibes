package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IntermediateChirography.class, GrizzlyBears.class, Shock.class, Murder.class})
class IntermediateChirographyTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a flying Inkling")
    void etbCreatesInkling() {
        harness.castFromHand(player1, new IntermediateChirography(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> inklings = controlledInklings(player1);
        assertThat(inklings).hasSize(1);
        assertThat(inklings.getFirst().getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(inklings.getFirst().getEffectivePower()).isEqualTo(2);
        assertThat(inklings.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("At level 2, the first life loss each turn puts a counter on a creature you control")
    void firstLifeLossPutsCounterOnceEachTurn() {
        Permanent chirography = harness.addToBattlefieldAndReturn(player1, new IntermediateChirography());
        chirography.setCounterCount(CounterType.LEVEL, 1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        loseLifeWithShock(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        loseLifeWithShock(player1);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("At level 3, a modified creature dying creates an Inkling at the next end step")
    void modifiedCreatureDeathCreatesInkling() {
        Permanent chirography = levelThreeChirography();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyWithMurder(player1, creature);
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(controlledInklings(player1)).hasSize(1);
    }

    @Test
    @DisplayName("An unmodified creature dying does not satisfy the level 3 ability")
    void unmodifiedCreatureDeathDoesNotCreateInkling() {
        levelThreeChirography();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroyWithMurder(player1, creature);
        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(controlledInklings(player1)).isEmpty();
    }

    private Permanent levelThreeChirography() {
        Permanent chirography = harness.addToBattlefieldAndReturn(player1, new IntermediateChirography());
        chirography.setCounterCount(CounterType.LEVEL, 2);
        return chirography;
    }

    private void loseLifeWithShock(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new Shock()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.castInstant(player, 0, player.getId());
        harness.passBothPriorities();
    }

    private void destroyWithMurder(Player player, Permanent creature) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new Murder()));
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castInstant(player, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private List<Permanent> controlledInklings(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Inkling"))
                .toList();
    }
}
