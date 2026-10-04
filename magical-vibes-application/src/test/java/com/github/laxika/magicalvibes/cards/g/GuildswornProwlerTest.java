package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuildswornProwler.class, GrizzlyBears.class, HillGiant.class, WrathOfGod.class})
class GuildswornProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies without blocking, it draws a card")
    void diesWithoutBlockingDrawsCard() {
        harness.addToBattlefield(player1, new GuildswornProwler());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        int handSizeBeforeCast = gd.playerHands.get(player1.getId()).size() + 1;
        harness.passBothPriorities();

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Guildsworn Prowler"));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBeforeCast);
    }

    @Test
    @DisplayName("When it dies blocking, it does not draw a card")
    void diesBlockingDoesNotDrawCard() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GuildswornProwler());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Guildsworn Prowler"));
        harness.assertNotOnBattlefield(player2, "Guildsworn Prowler");
    }

    @Test
    @DisplayName("Dying while attacking draws a card and deathtouch kills a larger blocker")
    void attackingDeathDrawsAndKillsLargerBlocker() {
        addCreatureReady(player1, new GuildswornProwler());
        addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Guildsworn Prowler");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Blocking Prowler kills a larger attacker without drawing for either player")
    void blockingDeathDoesNotDrawAndKillsLargerAttacker() {
        addCreatureReady(player1, new HillGiant());
        addCreatureReady(player2, new GuildswornProwler());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Guildsworn Prowler");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Simultaneous deaths draw one card for each nonblocking Prowler's controller")
    void simultaneousDeathsDrawForBothControllers() {
        harness.addToBattlefield(player1, new GuildswornProwler());
        harness.addToBattlefield(player2, new GuildswornProwler());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new HillGiant()));

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Guildsworn Prowler");
        harness.assertInGraveyard(player2, "Guildsworn Prowler");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Hill Giant");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
