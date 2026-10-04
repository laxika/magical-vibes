package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LastGasp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CyclopeanSnare.class, Forest.class, BorosRecruit.class, LastGasp.class})
class CyclopeanSnareTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target creature and returns itself to its owner's hand")
    void tapsCreatureAndReturnsToHand() {
        Permanent snare = harness.addToBattlefieldAndReturn(player1, new CyclopeanSnare());
        Permanent creature = addCreatureReady(player2, new BorosRecruit());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(snare.isTapped()).isTrue();
        harness.assertNotInHand(player1, "Cyclopean Snare");

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Cyclopean Snare");
        harness.assertInHand(player1, "Cyclopean Snare");
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhileTapped() {
        Permanent snare = harness.addToBattlefieldAndReturn(player1, new CyclopeanSnare());
        Permanent creature = addCreatureReady(player2, new BorosRecruit());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(snare.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent snare = harness.addToBattlefieldAndReturn(player1, new CyclopeanSnare());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");

        assertThat(snare.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Returns itself even when the target creature is already tapped")
    void returnsToHandWithAlreadyTappedTarget() {
        harness.addToBattlefield(player1, new CyclopeanSnare());
        Permanent creature = addCreatureReady(player2, new BorosRecruit());
        creature.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Cyclopean Snare");
        harness.assertInHand(player1, "Cyclopean Snare");
    }

    @Test
    @DisplayName("Does not return itself when its only target dies before resolution")
    void staysOnBattlefieldWhenTargetDies() {
        Permanent snare = harness.addToBattlefieldAndReturn(player1, new CyclopeanSnare());
        Permanent creature = addCreatureReady(player2, new BorosRecruit());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player2, List.of(new LastGasp()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.assertInGraveyard(player2, "Boros Recruit");
        harness.passBothPriorities();

        assertThat(snare.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Cyclopean Snare");
        harness.assertNotInHand(player1, "Cyclopean Snare");
    }

    @Test
    @DisplayName("Cannot activate with less than three mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent snare = harness.addToBattlefieldAndReturn(player1, new CyclopeanSnare());
        Permanent creature = addCreatureReady(player2, new BorosRecruit());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(snare.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        harness.assertNotInHand(player1, "Cyclopean Snare");
    }

    @Test
    @DisplayName("Returns a controlled Snare to its owner rather than its controller")
    void returnsToOwnerHand() {
        CyclopeanSnare snare = new CyclopeanSnare();
        snare.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, snare);
        Permanent creature = addCreatureReady(player1, new BorosRecruit());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Cyclopean Snare");
        harness.assertInHand(player2, "Cyclopean Snare");
        harness.assertNotInHand(player1, "Cyclopean Snare");
    }
}
