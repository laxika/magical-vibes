package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CabalStronghold.class, Swamp.class, Plains.class})
class CabalStrongholdTest extends BaseCardTest {

    @Test
    @DisplayName("First ability taps for colorless mana")
    void firstAbilityAddsColorless() {
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new CabalStronghold());

        stronghold.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability adds B for each basic Swamp controlled")
    void secondAbilityAddsBlackPerBasicSwamp() {
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new CabalStronghold());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        stronghold.setSummoningSick(false);

        int strongholdIdx = gd.playerBattlefields.get(player1.getId()).indexOf(stronghold);

        // Pay {3} mana cost
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, strongholdIdx, 1, null, null);

        // 3 basic Swamps = 3 black mana
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
    }

    @Test
    @DisplayName("Second ability with no Swamps adds zero black mana")
    void secondAbilityWithNoSwampsAddsNothing() {
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new CabalStronghold());

        stronghold.setSummoningSick(false);

        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("Second ability does not count non-basic lands with Swamp subtype")
    void secondAbilityDoesNotCountNonBasicSwamps() {
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new CabalStronghold());

        // Add a basic Swamp
        harness.addToBattlefield(player1, new Swamp());

        // Add a non-basic Swamp (simulate by stripping BASIC supertype)
        Swamp nonBasicSwamp = new Swamp();
        nonBasicSwamp.setSupertypes(Set.of());
        harness.addToBattlefield(player1, nonBasicSwamp);

        stronghold.setSummoningSick(false);

        int strongholdIdx = gd.playerBattlefields.get(player1.getId()).indexOf(stronghold);

        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, strongholdIdx, 1, null, null);

        // Only 1 basic Swamp counted, non-basic Swamp ignored
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability does not count opponent's basic Swamps")
    void secondAbilityDoesNotCountOpponentSwamps() {
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new CabalStronghold());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        stronghold.setSummoningSick(false);

        int strongholdIdx = gd.playerBattlefields.get(player1.getId()).indexOf(stronghold);

        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, strongholdIdx, 1, null, null);

        // Only 1 Swamp owned by player1, opponent's 2 Swamps not counted
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability ignores other basic land types and counts tapped Swamps")
    void secondAbilityCountsOnlyBasicSwampsRegardlessOfTapState() {
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new CabalStronghold());
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        swamp.tap();
        harness.addToBattlefield(player1, new Plains());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(stronghold.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("First ability taps a newly entered land and resolves immediately")
    void firstAbilityNeedsNoWaitingTurnAndUsesNoStack() {
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new CabalStronghold());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(stronghold.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
