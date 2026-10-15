package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.h.HandOfCruelty;
import com.github.laxika.magicalvibes.cards.o.ONaginata;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KagemarosClutch.class, HandOfCruelty.class, ONaginata.class})
class KagemarosClutchTest extends BaseCardTest {

    @Test
    @DisplayName("Casting the Aura counts only cards remaining in its controller's hand")
    void castingCountsRemainingHandAndAttachesToOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HandOfCruelty());
        harness.setHand(player1, List.of(new KagemarosClutch(), new ONaginata()));
        harness.setHand(player2, List.of(new ONaginata(), new ONaginata(), new ONaginata()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Kagemaro's Clutch").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Hand of Cruelty");
    }

    @Test
    @DisplayName("Casting the last card in hand gives no penalty")
    void castingLastCardGivesNoPenalty() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HandOfCruelty());
        harness.setHand(player1, List.of(new KagemarosClutch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kagemaro's Clutch");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("A hand increase kills a creature at zero toughness and puts its Aura in the graveyard")
    void handIncreaseKillsCreatureAndRemovesAura() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HandOfCruelty());
        harness.setHand(player1, List.of(new KagemarosClutch(), new ONaginata()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Hand of Cruelty");

        harness.setHand(player1, List.of(new ONaginata(), new ONaginata()));
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Hand of Cruelty");
        harness.assertInGraveyard(player2, "Hand of Cruelty");
        harness.assertNotOnBattlefield(player1, "Kagemaro's Clutch");
        harness.assertInGraveyard(player1, "Kagemaro's Clutch");
    }

    @Test
    @DisplayName("Kagemaro's Clutch gives the enchanted creature -X/-X for cards in the Aura controller's hand")
    void givesNegativeBoostBasedOnAuraControllersHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HandOfCruelty());

        harness.setHand(player1, List.of(new ONaginata(), new ONaginata(), new ONaginata()));
        harness.setHand(player2, List.of(new ONaginata()));

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new KagemarosClutch());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(-1);
    }

    @Test
    @DisplayName("Kagemaro's Clutch updates when the Aura controller's hand changes")
    void updatesDynamicallyWithHandSize() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HandOfCruelty());
        harness.setHand(player1, List.of());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new KagemarosClutch());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.setHand(player1, List.of(new ONaginata(), new ONaginata()));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(0);
    }

    @Test
    @DisplayName("Kagemaro's Clutch stops affecting the creature when it leaves the battlefield")
    void effectEndsWhenAuraLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HandOfCruelty());
        harness.setHand(player1, List.of(new ONaginata(), new ONaginata()));

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new KagemarosClutch());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(0);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Kagemaro's Clutch cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new ONaginata());
        harness.setHand(player1, List.of(new KagemarosClutch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent artifact = findPermanent(player1, "O-Naginata");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
