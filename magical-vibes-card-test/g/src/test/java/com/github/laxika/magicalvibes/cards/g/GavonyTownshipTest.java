package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BertaWiseExtrapolator;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({GavonyTownship.class, WalkingCorpse.class, BertaWiseExtrapolator.class})
class GavonyTownshipTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds colorless mana")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new GavonyTownship());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanent(player1, "Gavony Township").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counter ability puts +1/+1 on each creature you control")
    void counterAbilityPutsCountersOnOwnCreatures() {
        harness.addToBattlefield(player1, new GavonyTownship());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        List<Permanent> creatures = findPermanents(player1, "Walking Corpse");

        assertThat(creatures).hasSize(2);
        for (Permanent creature : creatures) {
            assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
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
        resolveAllTriggers();

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
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent opponentCreature = findPermanent(player2, "Walking Corpse");

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Counter ability stacks on multiple activations")
    void counterAbilityStacksOnMultipleActivations() {
        harness.addToBattlefield(player1, new GavonyTownship());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // First activation
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Add more mana for second activation
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Untap the land for second activation
        Permanent township = findPermanent(player1, "Gavony Township");
        township.untap();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Walking Corpse");

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getEffectivePower()).isEqualTo(4);   // 2 base + 2 counters
        assertThat(creature.getEffectiveToughness()).isEqualTo(4); // 2 base + 2 counters
    }
    @Test
    @DisplayName("Counter ability affects creatures present at resolution and ignores lands")
    void counterAbilityUsesBattlefieldAtResolution() {
        Permanent township = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(township.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(township.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counter ability resolves with no creatures")
    void counterAbilityResolvesWithNoCreatures() {
        Permanent township = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(township.isTapped()).isTrue();
        assertThat(township.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counter ability requires both green and white mana")
    void counterAbilityCannotUseOnlyGenericMana() {
        Permanent township = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(township.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate counter ability after tapping for mana")
    void counterAbilityRequiresUntappedTownship() {
        harness.addToBattlefield(player1, new GavonyTownship());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.stack).isEmpty();
    }
}
