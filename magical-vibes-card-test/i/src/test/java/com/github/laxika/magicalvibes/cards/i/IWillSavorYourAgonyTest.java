package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;

import com.github.laxika.magicalvibes.cards.c.ConsecratedSphinx;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IWillSavorYourAgony.class, GrizzlyBears.class, Forest.class, ConsecratedSphinx.class})
class IWillSavorYourAgonyTest extends BaseCardTest {

    private static final String DESTROY = "Destroy target creature.";
    private static final String DRAW = "Target player draws a card.";
    private static final String GAIN = "Target player gains 5 life.";

    @Test
    void resolvesEachSelectedModeWithItsOwnTarget() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 10);

        resolveScheme();

        harness.handleListChoice(player1, DESTROY);
        harness.handleListChoice(player1, DRAW);
        harness.handleListChoice(player1, GAIN);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        assertThat(gd.interaction.activeInteraction(com.github.laxika.magicalvibes.model.PendingInteraction.PermanentChoice.class)
                .validIds()).contains(player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
    }

    @Test
    void allowsChoosingTheSameModeThreeTimes() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        Permanent third = addCreatureReady(player2, new GrizzlyBears());

        resolveScheme();

        harness.handleListChoice(player1, DESTROY);
        harness.handleListChoice(player1, DESTROY);
        harness.handleListChoice(player1, DESTROY);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, third.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
    }

    @Test
    void repeatedDrawsUseEachSelectedPlayer() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        int firstHand = gd.playerHands.get(player1.getId()).size();
        int secondHand = gd.playerHands.get(player2.getId()).size();

        resolveScheme();
        harness.handleListChoice(player1, DRAW);
        harness.handleListChoice(player1, DRAW);
        harness.handleListChoice(player1, DRAW);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(firstHand + 2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(secondHand + 1);
    }

    @Test
    void repeatedLifeGainUsesEachSelectedPlayerWithoutCreatures() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        resolveScheme();
        harness.handleListChoice(player1, GAIN);
        harness.handleListChoice(player1, GAIN);
        harness.handleListChoice(player1, GAIN);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
    }

    @Test
    void resolvesModesInPrintedOrderEvenWhenDrawIsChosenFirst() {
        Permanent sphinx = addCreatureReady(player2, new ConsecratedSphinx());
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player1, 10);

        resolveScheme();
        harness.handleListChoice(player1, DRAW);
        harness.handleListChoice(player1, DESTROY);
        harness.handleListChoice(player1, GAIN);
        harness.handlePermanentChosen(player1, sphinx.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Consecrated Sphinx")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertLife(player1, 15);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    /**
     * Sets the scheme in motion: its "when you set this scheme in motion" ability goes on the stack through the stack
     * door, which has its modes chosen as it does (CR 603.3c).
     */
    private void resolveScheme() {
        IWillSavorYourAgony scheme = new IWillSavorYourAgony();
        TriggeredAbilityStackService door = GameTestEngineContext.get().getBean(TriggeredAbilityStackService.class);
        harness.inMutationScope(() -> {
            door.putMade(gd, TriggeredAbilityStackService.madeAbility(scheme, player1.getId(), scheme.getName(),
                    scheme.getCastingCostsAndSpellEffects()));
            harness.getTriggerCollectionService().beginStackDoorChoices(gd);
        });
    }
}
