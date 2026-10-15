package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HerosDownfall;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReaperOfTheWilds.class, TravelingPhilosopher.class, LightningStrike.class, HerosDownfall.class})
class ReaperOfTheWildsTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature dying makes Reaper of the Wilds scry 1")
    void anotherCreatureDiesTriggersScry() {
        addReaper(player1);
        harness.addToBattlefield(player1, new TravelingPhilosopher());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        UUID philosopherId = harness.getPermanentId(player1, "Traveling Philosopher");
        harness.castAndResolveInstant(player2, 0, philosopherId);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Reaper of the Wilds does not trigger when it dies")
    void ownDeathDoesNotTrigger() {
        addReaper(player1);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new HerosDownfall()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        UUID reaperId = harness.getPermanentId(player1, "Reaper of the Wilds");
        harness.castAndResolveInstant(player2, 0, reaperId);

        assertThat(countPermanents(player1, "Reaper of the Wilds")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof ReaperOfTheWilds);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Black ability grants deathtouch until end of turn")
    void blackAbilityGrantsDeathtouchUntilEndOfTurn() {
        Permanent reaper = addReaper(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, reaper, Keyword.DEATHTOUCH)).isTrue();

        forceEndStep();

        assertThat(gqs.hasKeyword(gd, reaper, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Green ability grants hexproof until end of turn")
    void greenAbilityGrantsHexproofUntilEndOfTurn() {
        Permanent reaper = addReaper(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, reaper, Keyword.HEXPROOF)).isTrue();

        forceEndStep();

        assertThat(gqs.hasKeyword(gd, reaper, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("An opposing creature dying scries the Reaper controller's library")
    void opposingCreatureDiesScriesControllersLibrary() {
        addReaper(player1);
        harness.addToBattlefield(player2, new TravelingPhilosopher());
        TravelingPhilosopher top = new TravelingPhilosopher();
        LightningStrike bottom = new LightningStrike();
        harness.setLibrary(player1, List.of(top, bottom));
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Traveling Philosopher"));
        resolveAllTriggers();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom, top);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The scry trigger resolves even if Reaper dies in response")
    void scryResolvesAfterReaperDies() {
        addReaper(player1);
        harness.addToBattlefield(player1, new TravelingPhilosopher());
        TravelingPhilosopher top = new TravelingPhilosopher();
        harness.setLibrary(player1, List.of(top));
        setupPlayer2Active();
        harness.setHand(player2, List.of(new LightningStrike(), new HerosDownfall()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Traveling Philosopher"));
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Reaper of the Wilds"));
        assertThat(countPermanents(player1, "Reaper of the Wilds")).isZero();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("Hexproof activated in response makes opposing removal's target illegal")
    void hexproofInResponseStopsOpposingRemoval() {
        Permanent reaper = addReaper(player1);
        setupPlayer2Active();
        harness.setHand(player2, List.of(new HerosDownfall()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, reaper.getId());
        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Reaper of the Wilds")).isSameAs(reaper);
        assertThat(gqs.hasKeyword(gd, reaper, Keyword.HEXPROOF)).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof HerosDownfall);
    }

    private Permanent addReaper(Player player) {
        return addCreatureReady(player, new ReaperOfTheWilds());
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void forceEndStep() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
