package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BertaWiseExtrapolator;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

class GavonyTownshipTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has two activated abilities")
    void hasTwoActivatedAbilities() {
        GavonyTownship card = new GavonyTownship();

        assertThat(card.getActivatedAbilities()).hasSize(2);
    }

    

    

    // ===== Mana ability =====

    @Test
    @DisplayName("Tapping for mana adds colorless mana")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new GavonyTownship());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
    }

    // ===== Counter ability =====

    @Test
    @DisplayName("Counter ability puts +1/+1 on each creature you control")
    void counterAbilityPutsCountersOnOwnCreatures() {
        harness.addToBattlefield(player1, new GavonyTownship());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        List<Permanent> bears = findPermanents(player1, "Grizzly Bears");

        assertThat(bears).hasSize(2);
        for (Permanent bear : bears) {
            assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Counter ability fires a creature's +1/+1 counter-placement trigger")
    void counterAbilityFiresOnCounterPlacedTriggers() {
        harness.addToBattlefield(player1, new GavonyTownship());
        Permanent berta = harness.addToBattlefieldAndReturn(player1, new BertaWiseExtrapolator());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities(); // counter ability resolves; Berta's trigger goes on the stack
        harness.passBothPriorities(); // Berta's mana trigger resolves

        GameData gd = harness.getGameData();
        assertThat(berta.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        int before = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);
        harness.handleListChoice(player1, "BLUE");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(before + 1);
    }

    @Test
    @DisplayName("Counter ability does not affect opponent's creatures")
    void counterAbilityDoesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new GavonyTownship());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent opponentBear = findPermanent(player2, "Grizzly Bears");

        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Counter ability stacks on multiple activations")
    void counterAbilityStacksOnMultipleActivations() {
        harness.addToBattlefield(player1, new GavonyTownship());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // First activation
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Add more mana for second activation (next turn)
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Untap the land for second activation
        Permanent township = findPermanent(player1, "Gavony Township");
        township.untap();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bear.getEffectivePower()).isEqualTo(4);   // 2 base + 2 counters
        assertThat(bear.getEffectiveToughness()).isEqualTo(4); // 2 base + 2 counters
    }
}
