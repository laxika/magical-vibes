package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TruefirePaladin.class})
class TruefirePaladinTest extends BaseCardTest {

    @Test
    @DisplayName("First ability gives +2/+0 until end of turn")
    void pumpAbility() {
        Permanent paladin = addPaladin(player1);
        addRedWhite(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, paladin)).isEqualTo(2);
    }

    @Test
    @DisplayName("The +2/+0 boost wears off at end of turn")
    void pumpWearsOff() {
        Permanent paladin = addPaladin(player1);
        addRedWhite(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Second ability grants first strike until end of turn")
    void firstStrikeAbility() {
        Permanent paladin = addPaladin(player1);
        addRedWhite(player1);

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.FIRST_STRIKE)).isFalse();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Granted first strike wears off at end of turn")
    void firstStrikeWearsOff() {
        Permanent paladin = addPaladin(player1);
        addRedWhite(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Both abilities can be activated in the same turn")
    void bothAbilitiesStack() {
        Permanent paladin = addPaladin(player1);
        addRedWhite(player1);
        addRedWhite(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without both red and white mana")
    void cannotActivateWithoutMana() {
        addPaladin(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Repeated pump activations accumulate and affect only their source")
    void repeatedPumpsAffectOnlySource() {
        Permanent paladin = addPaladin(player1);
        Permanent other = addPaladin(player1);
        addRedWhite(player1);
        addRedWhite(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, paladin)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Both abilities can be used while tapped and summoning sick")
    void abilitiesDoNotRequireTappingOrHaste() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new TruefirePaladin());
        paladin.setSummoningSick(true);
        paladin.tap();
        addRedWhite(player1);
        addRedWhite(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(paladin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("First strike requires white mana as well as red mana")
    void firstStrikeCannotBePaidWithOnlyRed() {
        addPaladin(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activated abilities do not affect a new permanent after their source leaves")
    void abilitiesDoNotFollowReturnedSource() {
        Permanent original = addPaladin(player1);
        addRedWhite(player1);
        addRedWhite(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 1, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, original.getCard());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Vigilance allows attacking without tapping")
    void attackingDoesNotTap() {
        Permanent paladin = addPaladin(player1);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(paladin.isAttacking()).isTrue();
        assertThat(paladin.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Granted first strike kills a blocker before it can deal damage")
    void firstStrikeWinsMirrorCombat() {
        Permanent paladin = addPaladin(player1);
        harness.addToBattlefield(player2, new TruefirePaladin());
        addRedWhite(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Truefire Paladin");
        harness.assertInGraveyard(player2, "Truefire Paladin");
        assertThat(paladin.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    private void addRedWhite(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
    }

    private Permanent addPaladin(Player player) {
        return addCreatureReady(player, new TruefirePaladin());
    }
}
