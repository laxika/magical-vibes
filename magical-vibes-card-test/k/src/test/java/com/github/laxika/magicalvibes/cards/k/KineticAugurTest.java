package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.c.CrashThrough;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KineticAugur.class, AlpineWatchdog.class, CrashThrough.class, Shock.class, Opt.class, Plains.class, Forest.class, Mountain.class})
class KineticAugurTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals instant and sorcery cards in its controller's graveyard and toughness stays 4")
    void powerCountsOwnInstantsAndSorceries() {
        Permanent augur = harness.addToBattlefieldAndReturn(player1, new KineticAugur());
        harness.setGraveyard(player1, List.of(new Shock(), new Opt(), new CrashThrough(), new Plains(), new AlpineWatchdog()));

        assertThat(gqs.getEffectivePower(gd, augur)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, augur)).isEqualTo(4);
    }

    @Test
    @DisplayName("When it enters, its controller discards up to two cards and draws that many")
    void entersWithRummage() {
        Card discardOne = new AlpineWatchdog();
        Card discardTwo = new AlpineWatchdog();
        Card kept = new AlpineWatchdog();
        Card drawOne = new Forest();
        Card drawTwo = new Mountain();
        harness.setLibrary(player1, List.of(drawOne, drawTwo));
        harness.setHand(player1, List.of(new KineticAugur(), discardOne, discardTwo, kept));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(kept, drawOne, drawTwo);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(discardOne, discardTwo);
    }

    @Test
    @DisplayName("The enter-the-battlefield ability may discard zero cards")
    void mayDiscardZeroCards() {
        Card kept = new AlpineWatchdog();
        harness.setHand(player1, List.of(new KineticAugur(), kept));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
    @Test
    void powerUpdatesAndIgnoresOpponentsGraveyard() {
        Permanent augur = harness.addToBattlefieldAndReturn(player1, new KineticAugur());
        harness.setGraveyard(player2, List.of(new Shock(), new CrashThrough()));
        assertThat(gqs.getEffectivePower(gd, augur)).isZero();

        harness.setGraveyard(player1, List.of(new Shock(), new CrashThrough()));
        assertThat(gqs.getEffectivePower(gd, augur)).isEqualTo(2);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, augur)).isZero();
    }

    @Test
    void mayDiscardOnlyCardAndDrawOne() {
        Card discarded = new Shock();
        Card drawn = new Forest();
        Card undrawn = new Mountain();
        harness.setLibrary(player1, List.of(drawn, undrawn));
        harness.setHand(player1, List.of(new KineticAugur(), discarded));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Kinetic Augur"))).isEqualTo(1);
    }

    @Test
    void emptyHandDrawsNothingAndNeedsNoChoice() {
        Card undrawn = new Forest();
        harness.setLibrary(player1, List.of(undrawn));
        harness.setHand(player1, List.of(new KineticAugur()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
    }

    @Test
    void trampleDealsExcessDamageToDefendingPlayer() {
        Permanent augur = addCreatureReady(player1, new KineticAugur());
        addCreatureReady(player2, new AlpineWatchdog());
        harness.setGraveyard(player1, List.of(new Shock(), new Opt(), new CrashThrough()));
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(augur);
        harness.assertInGraveyard(player2, "Alpine Watchdog");
    }
}
