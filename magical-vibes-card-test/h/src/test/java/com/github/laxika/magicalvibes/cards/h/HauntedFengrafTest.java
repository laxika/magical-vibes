package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.cards.t.TragicSlip;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HauntedFengraf.class, DawntreaderElk.class, TragicSlip.class})
class HauntedFengrafTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana adds {C}")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new HauntedFengraf());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating sacrifice ability puts the return on the stack")
    void activatingPutsOnStack() {
        harness.addToBattlefield(player1, new HauntedFengraf());
        harness.setGraveyard(player1, List.of(new DawntreaderElk()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Haunted Fengraf");
    }

    @Test
    @DisplayName("Haunted Fengraf is sacrificed as a cost before resolution")
    void sacrificedBeforeResolution() {
        harness.addToBattlefield(player1, new HauntedFengraf());
        harness.setGraveyard(player1, List.of(new DawntreaderElk()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Haunted Fengraf");
        harness.assertInGraveyard(player1, "Haunted Fengraf");
    }

    @Test
    @DisplayName("Mana is consumed when activating sacrifice ability")
    void manaIsConsumedWhenActivating() {
        harness.addToBattlefield(player1, new HauntedFengraf());
        harness.setGraveyard(player1, List.of(new DawntreaderElk()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving returns a creature card from graveyard to hand")
    void resolvingReturnsCreature() {
        harness.addToBattlefield(player1, new HauntedFengraf());
        harness.setGraveyard(player1, List.of(new DawntreaderElk()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Dawntreader Elk");
        harness.assertNotInGraveyard(player1, "Dawntreader Elk");
    }

    @Test
    @DisplayName("Returns exactly one creature at random when several are in the graveyard")
    void returnsOneRandomCreatureFromMultiple() {
        harness.addToBattlefield(player1, new HauntedFengraf());
        harness.setGraveyard(player1, List.of(new DawntreaderElk(), new DawntreaderElk()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        long handBears = gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Dawntreader Elk"))
                .count();
        assertThat(handBears).isEqualTo(1);

        long graveyardBears = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Dawntreader Elk"))
                .count();
        assertThat(graveyardBears).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-creature cards in the graveyard are ignored")
    void ignoresNonCreatures() {
        harness.addToBattlefield(player1, new HauntedFengraf());
        harness.setGraveyard(player1, List.of(new TragicSlip(), new DawntreaderElk()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // The creature is returned, the instant stays in the graveyard
        harness.assertInHand(player1, "Dawntreader Elk");
        harness.assertInGraveyard(player1, "Tragic Slip");
        harness.assertNotInHand(player1, "Tragic Slip");
    }

    @Test
    @DisplayName("Resolves without returning anything when no creatures are in the graveyard")
    void doesNothingWithNoCreatures() {
        harness.addToBattlefield(player1, new HauntedFengraf());
        harness.setGraveyard(player1, List.of(new TragicSlip()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Tragic Slip");
        harness.assertNotInHand(player1, "Tragic Slip");
    }

    @Test
    @DisplayName("A creature entering the graveyard after activation can be returned")
    void usesGraveyardAtResolution() {
        harness.addToBattlefield(player1, new HauntedFengraf());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 1, null, null);

        gd.playerGraveyards.get(player1.getId()).add(new DawntreaderElk());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Dawntreader Elk");
        harness.assertNotInGraveyard(player1, "Dawntreader Elk");
        harness.assertInGraveyard(player1, "Haunted Fengraf");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creatures in an opponent's graveyard are not returned")
    void ignoresOpponentsGraveyard() {
        harness.addToBattlefield(player1, new HauntedFengraf());
        harness.setGraveyard(player2, List.of(new DawntreaderElk()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dawntreader Elk");
        harness.assertNotInHand(player1, "Dawntreader Elk");
        harness.assertNotInHand(player2, "Dawntreader Elk");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate sacrifice ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new HauntedFengraf());
        harness.setGraveyard(player1, List.of(new DawntreaderElk()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate sacrifice ability when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new HauntedFengraf());
        harness.setGraveyard(player1, List.of(new DawntreaderElk()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Tap for mana first
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
