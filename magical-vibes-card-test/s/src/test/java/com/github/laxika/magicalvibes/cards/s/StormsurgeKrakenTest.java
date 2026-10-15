package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CurtainOfLight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YargleGluttonOfUrborg;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormsurgeKraken.class, GrizzlyBears.class, YargleGluttonOfUrborg.class, CurtainOfLight.class})
class StormsurgeKrakenTest extends BaseCardTest {

    @Test
    @DisplayName("Lieutenant gives Stormsurge Kraken +2/+2 while its controller controls a commander")
    void lieutenantBonusAppliesWhileControllingCommander() {
        YargleGluttonOfUrborg commander = new YargleGluttonOfUrborg();
        gd.makeCommander(player1.getId(), commander);
        Permanent kraken = addCreatureReady(player1, new StormsurgeKraken());
        harness.addToBattlefield(player1, commander);

        assertThat(gqs.getEffectivePower(gd, kraken)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, kraken)).isEqualTo(7);
    }

    @Test
    @DisplayName("Lieutenant bonus disappears when the commander leaves the battlefield")
    void lieutenantBonusDisappearsWithoutCommander() {
        YargleGluttonOfUrborg commander = new YargleGluttonOfUrborg();
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
        addCreatureReady(player1, new StormsurgeKraken());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Stormsurge Kraken"));
    }

    @Test
    @DisplayName("Becoming blocked presents a may-draw-two choice")
    void becomingBlockedMayDrawTwoCards() {
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        addCreatureReady(player1, new StormsurgeKraken());
        addCommander(player1);
        addCreatureReady(player2, new GrizzlyBears());

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackersAndPrepareBlockers(List.of(0));
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
        addCreatureReady(player1, new StormsurgeKraken());
        addCommander(player1);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("The draw-two choice may be declined")
    void mayDeclineDrawingCards() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent kraken = addCreatureReady(player1, new StormsurgeKraken());
        kraken.setAttacking(true);
        addCommander(player1);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An already triggered draw ability survives losing control of the commander")
    void drawTriggerResolvesAfterCommanderControlIsLost() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent kraken = addCreatureReady(player1, new StormsurgeKraken());
        kraken.setAttacking(true);
        YargleGluttonOfUrborg commander = new YargleGluttonOfUrborg();
        gd.makeCommander(player1.getId(), commander);
        Permanent commanderPermanent = harness.addToBattlefieldAndReturn(player1, commander);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        gd.playerBattlefields.get(player1.getId()).remove(commanderPermanent);
        gd.playerBattlefields.get(player2.getId()).add(commanderPermanent);

        assertThat(gqs.getEffectivePower(gd, kraken)).isEqualTo(5);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Controlling another player's commander does not enable lieutenant")
    void opponentsCommanderDoesNotEnableLieutenant() {
        YargleGluttonOfUrborg commander = new YargleGluttonOfUrborg();
        gd.makeCommander(player2.getId(), commander);
        Permanent kraken = addCreatureReady(player1, new StormsurgeKraken());
        harness.addToBattlefield(player1, commander);
        kraken.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, kraken)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, kraken)).isEqualTo(5);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(kraken.getCard().getId()));
    }

    @Test
    @DisplayName("A commander outside the battlefield does not enable lieutenant")
    void commanderInCommandZoneDoesNotEnableLieutenant() {
        YargleGluttonOfUrborg commander = new YargleGluttonOfUrborg();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        Permanent kraken = addCreatureReady(player1, new StormsurgeKraken());
        kraken.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, kraken)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, kraken)).isEqualTo(5);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(kraken.getCard().getId()));
    }

    @Test
    @DisplayName("Becoming blocked without blockers still triggers the lieutenant draw ability")
    void becomingBlockedByCurtainOfLightTriggersDraw() {
        harness.setHand(player1, List.of(new CurtainOfLight()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent kraken = addCreatureReady(player1, new StormsurgeKraken());
        kraken.setAttacking(true);
        addCommander(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));

        harness.castAndResolveInstant(player1, 0, kraken.getId());

        assertThat(kraken.isBlockedWithoutBlockers()).isTrue();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(kraken.getCard().getId()));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Hexproof prevents the opponent from targeting Kraken with Curtain of Light")
    void opponentCannotTargetKraken() {
        harness.setHand(player2, List.of(new CurtainOfLight()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        addCreatureReady(player1, new StormsurgeKraken());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        Permanent kraken = findPermanent(player1, "Stormsurge Kraken");

        assertThatThrownBy(() -> harness.castInstant(player2, 0, kraken.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    private void addCommander(com.github.laxika.magicalvibes.model.Player player) {
        YargleGluttonOfUrborg commander = new YargleGluttonOfUrborg();
        gd.makeCommander(player.getId(), commander);
        harness.addToBattlefield(player, commander);
    }

}
