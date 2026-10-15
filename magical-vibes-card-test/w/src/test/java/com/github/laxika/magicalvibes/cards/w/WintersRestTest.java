package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WintersRest.class, MotherBear.class, SnowCoveredForest.class})
class WintersRestTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to and taps the target creature")
    void entersAttachedAndTapsTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MotherBear());

        castWintersRest(creature);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Winter's Rest").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Does not lock the creature without another snow permanent")
    void creatureUntapsWithoutSnowPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MotherBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WintersRest());
        aura.setAttachedTo(creature.getId());
        creature.tap();

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Locks the enchanted creature while its controller has another snow permanent")
    void creatureDoesNotUntapWithSnowPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MotherBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WintersRest());
        aura.setAttachedTo(creature.getId());
        harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        creature.tap();

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's snow permanent does not satisfy the condition")
    void opponentSnowPermanentDoesNotEnableLock() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MotherBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WintersRest());
        aura.setAttachedTo(creature.getId());
        harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        creature.tap();

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The lock updates when another snow permanent enters and leaves")
    void lockUpdatesWithSnowPermanentPresence() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MotherBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WintersRest());
        aura.setAttachedTo(creature.getId());
        creature.tap();

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();

        Permanent snow = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        creature.tap();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(snow);
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A second Winter's Rest satisfies the other snow permanent condition")
    void anotherWintersRestEnablesBothLocks() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new MotherBear());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MotherBear());
        Permanent firstAura = harness.addToBattlefieldAndReturn(player1, new WintersRest());
        Permanent secondAura = harness.addToBattlefieldAndReturn(player1, new WintersRest());
        firstAura.setAttachedTo(first.getId());
        secondAura.setAttachedTo(second.getId());
        first.tap();
        second.tap();

        harness.performUntapStep(player2);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The enter trigger taps the creature enchanted when it resolves")
    void enterTriggerFollowsCurrentAttachment() {
        Permanent original = harness.addToBattlefieldAndReturn(player2, new MotherBear());
        Permanent newHost = harness.addToBattlefieldAndReturn(player2, new MotherBear());
        castWintersRest(original);
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Winter's Rest");
        aura.setAttachedTo(newHost.getId());
        harness.passBothPriorities();

        assertThat(original.isTapped()).isFalse();
        assertThat(newHost.isTapped()).isTrue();
    }

    private void castWintersRest(Permanent target) {
        harness.setHand(player1, List.of(new WintersRest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
    }

}
