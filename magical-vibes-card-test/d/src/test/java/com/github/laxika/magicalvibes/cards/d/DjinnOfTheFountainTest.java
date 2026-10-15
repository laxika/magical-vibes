package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImpedeMomentum;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DjinnOfTheFountain.class, Shock.class, Forest.class, GrizzlyBears.class, ImpedeMomentum.class})
class DjinnOfTheFountainTest extends BaseCardTest {

    private static final String BOOST_MODE = "Djinn of the Fountain gets +1/+1 until end of turn";
    private static final String FLICKER_MODE =
            "Exile Djinn of the Fountain. Return it to the battlefield under its owner's control at the beginning of the next end step";
    private static final String SCRY_MODE = "Scry 1";

    @Test
    @DisplayName("Casting an instant lets Djinn of the Fountain get +1/+1 until end of turn")
    void boostMode() {
        Permanent djinn = addReadyDjinn();
        int initialPower = djinn.getEffectivePower();
        int initialToughness = djinn.getEffectiveToughness();

        castShock();
        harness.handleListChoice(player1, BOOST_MODE);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(initialPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(initialToughness + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(initialPower);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(initialToughness);
    }

    @Test
    @DisplayName("Flicker mode returns Djinn of the Fountain at the next end step")
    void flickerMode() {
        Permanent djinn = addReadyDjinn();
        UUID oldId = djinn.getId();

        castShock();
        harness.handleListChoice(player1, FLICKER_MODE);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Djinn of the Fountain");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Djinn of the Fountain");
        assertThat(returned.getId()).isNotEqualTo(oldId);
    }

    @Test
    @DisplayName("Scry mode starts a scry 1 interaction")
    void scryMode() {
        addReadyDjinn();
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        castShock();
        harness.handleListChoice(player1, SCRY_MODE);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(topCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Djinn of the Fountain")
    void creatureSpellDoesNotTrigger() {
        addReadyDjinn();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Delayed return uses the stack and leaves time to respond")
    void delayedReturnUsesStack() {
        addReadyDjinn();
        castShock();
        harness.handleListChoice(player1, FLICKER_MODE);
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);

        harness.assertNotOnBattlefield(player1, "Djinn of the Fountain");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Djinn of the Fountain");
    }

    @Test
    @DisplayName("Casting a sorcery triggers the boost before the spell resolves")
    void sorceryTriggers() {
        Permanent djinn = addReadyDjinn();
        int initialPower = gqs.getEffectivePower(gd, djinn);
        harness.setHand(player1, List.of(new ImpedeMomentum()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, djinn.getId());
        harness.handleListChoice(player1, BOOST_MODE);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(initialPower + 1);
        assertThat(gd.stack).hasSize(1);
        assertThat(djinn.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's instant does not trigger Djinn")
    void opponentsInstantDoesNotTrigger() {
        addReadyDjinn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Scry can put the top card on the bottom")
    void scryToBottom() {
        addReadyDjinn();
        Card top = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(top, second));
        castShock();
        harness.handleListChoice(player1, SCRY_MODE);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, top);
    }

    @Test
    @DisplayName("Scry with an empty library does not require a choice")
    void scryEmptyLibrary() {
        addReadyDjinn();
        harness.setLibrary(player1, List.of());
        castShock();
        harness.handleListChoice(player1, SCRY_MODE);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Flickering during an end step waits for the following turn's end step")
    void flickerDuringEndStep() {
        addReadyDjinn();
        harness.forceStep(TurnStep.END_STEP);
        castShock();
        harness.handleListChoice(player1, FLICKER_MODE);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Djinn of the Fountain");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.assertNotOnBattlefield(player1, "Djinn of the Fountain");
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Djinn of the Fountain");
    }

    @Test
    @DisplayName("A borrowed Djinn returns untapped under its owner's control")
    void flickerReturnsToOwner() {
        DjinnOfTheFountain card = new DjinnOfTheFountain();
        card.setOwnerId(player2.getId());
        Permanent djinn = addCreatureReady(player1, card);
        gd.stolenCreatures.put(djinn.getId(), player2.getId());
        djinn.tap();
        castShock();
        harness.handleListChoice(player1, FLICKER_MODE);
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Djinn of the Fountain");
        Permanent returned = findPermanent(player2, "Djinn of the Fountain");
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getId()).isNotEqualTo(djinn.getId());
    }

    private Permanent addReadyDjinn() {
        Permanent djinn = harness.addToBattlefieldAndReturn(player1, new DjinnOfTheFountain());
        djinn.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return djinn;
    }

    private void castShock() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
    }
}
