package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NevaStalkedByNightmares.class, GrizzlyBears.class, PhyrexianArena.class, Forest.class})
class NevaStalkedByNightmaresTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature card from the graveyard to hand when it enters")
    void returnsTargetCreatureCardToHand() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        castNeva();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns a target enchantment card from the graveyard to hand when it enters")
    void returnsTargetEnchantmentCardToHand() {
        Card enchantment = new PhyrexianArena();
        harness.setGraveyard(player1, List.of(enchantment));

        castNeva();

        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Phyrexian Arena");
        harness.assertNotInGraveyard(player1, "Phyrexian Arena");
    }

    @Test
    @DisplayName("Does not target a card that is neither a creature nor an enchantment")
    void doesNotTargetInvalidGraveyardCard() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land, creature));

        castNeva();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
    }

    @Test
    @DisplayName("Puts a counter on itself and scries when your enchantment goes to the graveyard")
    void controlledEnchantmentDyingAddsCounterAndScries() {
        Permanent neva = harness.addToBattlefieldAndReturn(player1, new NevaStalkedByNightmares());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new PhyrexianArena());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, enchantment));
        harness.passBothPriorities();

        assertThat(neva.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's enchantment")
    void opponentEnchantmentDoesNotTrigger() {
        Permanent neva = harness.addToBattlefieldAndReturn(player1, new NevaStalkedByNightmares());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, enchantment));

        assertThat(neva.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void castNeva() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NevaStalkedByNightmares()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
