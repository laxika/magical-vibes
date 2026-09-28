package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormsurgeKraken.class, GrizzlyBears.class})
class StormsurgeKrakenTest extends BaseCardTest {

    @Test
    @DisplayName("Lieutenant gives Stormsurge Kraken +2/+2 while its controller controls a commander")
    void lieutenantBonusAppliesWhileControllingCommander() {
        GrizzlyBears commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        Permanent kraken = addCreatureReady(player1, new StormsurgeKraken());
        harness.addToBattlefield(player1, commander);

        assertThat(gqs.getEffectivePower(gd, kraken)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, kraken)).isEqualTo(7);
    }

    @Test
    @DisplayName("Lieutenant bonus disappears when the commander leaves the battlefield")
    void lieutenantBonusDisappearsWithoutCommander() {
        GrizzlyBears commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        Permanent kraken = addCreatureReady(player1, new StormsurgeKraken());
        Permanent commanderPermanent = harness.addToBattlefieldAndReturn(player1, commander);

        gd.playerBattlefields.get(player1.getId()).remove(commanderPermanent);

        assertThat(gqs.getEffectivePower(gd, kraken)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, kraken)).isEqualTo(5);
    }

    @Test
    @DisplayName("Lieutenant ability is unavailable without a commander")
    void lieutenantAbilityIsUnavailableWithoutCommander() {
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        Permanent kraken = addCreatureReady(player1, new StormsurgeKraken());
        kraken.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Stormsurge Kraken"));
    }

    @Test
    @DisplayName("Becoming blocked presents a may-draw-two choice")
    void becomingBlockedMayDrawTwoCards() {
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        Permanent kraken = addCreatureReady(player1, new StormsurgeKraken());
        kraken.setAttacking(true);
        addCommander(player1);
        addCreatureReady(player2, new GrizzlyBears());

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures triggers only once")
    void multipleBlockersTriggerOnlyOnce() {
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        Permanent kraken = addCreatureReady(player1, new StormsurgeKraken());
        kraken.setAttacking(true);
        addCommander(player1);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    private void addCommander(com.github.laxika.magicalvibes.model.Player player) {
        GrizzlyBears commander = new GrizzlyBears();
        gd.makeCommander(player.getId(), commander);
        harness.addToBattlefield(player, commander);
    }

}
