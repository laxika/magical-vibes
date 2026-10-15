package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ScurryOak;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GracefulRestoration.class, GrizzlyBears.class, HillGiant.class, ScurryOak.class})
class GracefulRestorationTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureWithCounter() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new GracefulRestoration()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void returnsUpToTwoSmallCreatures() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        HillGiant tooLarge = new HillGiant();
        harness.setGraveyard(player1, List.of(first, second, tooLarge));
        harness.setHand(player1, List.of(new GracefulRestoration()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player1, 0, 1, List.of());
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getId())
                .contains(tooLarge.getId())
                .doesNotContain(first.getId(), second.getId());
    }

    @Test
    void secondModeRejectsCreatureWithPowerGreaterThanTwo() {
        HillGiant tooLarge = new HillGiant();
        harness.setGraveyard(player1, List.of(tooLarge));
        harness.setHand(player1, List.of(new GracefulRestoration()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Hill Giant")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(tooLarge);
    }

    @Test
    void firstModeCanReturnCreatureWithPowerGreaterThanTwo() {
        HillGiant creature = new HillGiant();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new GracefulRestoration()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Hill Giant")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    void firstModeCounterIsPresentWhenEntryTriggersAreChecked() {
        Permanent oak = harness.addToBattlefieldAndReturn(player1, new ScurryOak());
        oak.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new GracefulRestoration()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(oak.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void secondModeCanChooseZeroTargetsEvenWhenCreaturesAreAvailable() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new GracefulRestoration()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void secondModeCanReturnOnlyOneCreatureWithoutAddingCounters() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new GracefulRestoration()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second).doesNotContain(first);
    }

    @Test
    void secondModeStillReturnsRemainingLegalTarget() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new GracefulRestoration()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.setGraveyard(player1, List.of(second));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanent(player1, "Grizzly Bears").getCard().getId()).isEqualTo(second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(second);
    }
}
