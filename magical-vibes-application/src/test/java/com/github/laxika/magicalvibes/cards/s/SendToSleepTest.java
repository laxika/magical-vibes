package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SendToSleepTest extends BaseCardTest {

    private void castSendToSleep(List<UUID> targets) {
        harness.setHand(player1, List.of(new SendToSleep()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targets);
        harness.passBothPriorities();
    }

    private void fillGraveyardWithSpells() {
        harness.setGraveyard(player1, List.of(new LightningBolt(), new Shock()));
    }

    @Test
    @DisplayName("Taps both target creatures")
    void tapsTwoCreatures() {
        Permanent a = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent b = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSendToSleep(List.of(a.getId(), b.getId()));

        assertThat(a.isTapped()).isTrue();
        assertThat(b.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Without spell mastery the tapped creatures untap normally")
    void noUntapLockWithoutSpellMastery() {
        Permanent a = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent b = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSendToSleep(List.of(a.getId(), b.getId()));

        assertThat(a.getSkipUntapCount()).isZero();
        assertThat(b.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Spell mastery locks both creatures out of their next untap step")
    void spellMasteryLocksUntap() {
        fillGraveyardWithSpells();
        Permanent a = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent b = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSendToSleep(List.of(a.getId(), b.getId()));

        assertThat(a.isTapped()).isTrue();
        assertThat(b.isTapped()).isTrue();
        assertThat(a.getSkipUntapCount()).isEqualTo(1);
        assertThat(b.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("A single instant in the graveyard is not enough for spell mastery")
    void oneInstantIsNotSpellMastery() {
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        Permanent a = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSendToSleep(List.of(a.getId()));

        assertThat(a.isTapped()).isTrue();
        assertThat(a.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("May target only a single creature (up to two)")
    void tapsOneCreature() {
        fillGraveyardWithSpells();
        Permanent a = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSendToSleep(List.of(a.getId()));

        assertThat(a.isTapped()).isTrue();
        assertThat(a.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new SendToSleep()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID mountainId = mountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
