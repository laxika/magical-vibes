package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionBlack;
import com.github.laxika.magicalvibes.cards.e.EnergyStorm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BalduvianShamanTest extends BaseCardTest {

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Changes color word and grants cumulative upkeep {1}")
    void changesTextAndGrantsCumulativeUpkeep() {
        Permanent shaman = addReady(player1, new BalduvianShaman());
        Permanent cop = harness.addToBattlefieldAndReturn(player1, new CircleOfProtectionBlack());

        int shamanIdx = gd.playerBattlefields.get(player1.getId()).indexOf(shaman);
        harness.activateAbility(player1, shamanIdx, null, cop.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "BLUE");

        assertThat(cop.getTextReplacements()).containsExactly(new TextReplacement("black", "blue"));
        assertThat(cop.hasCumulativeUpkeep()).isTrue();
        assertThat(shaman.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted cumulative upkeep triggers and can be paid")
    void grantedCumulativeUpkeepTriggers() {
        Permanent shaman = addReady(player1, new BalduvianShaman());
        Permanent cop = harness.addToBattlefieldAndReturn(player1, new CircleOfProtectionBlack());

        int shamanIdx = gd.playerBattlefields.get(player1.getId()).indexOf(shaman);
        harness.activateAbility(player1, shamanIdx, null, cop.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "RED");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(cop.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cop);
    }

    @Test
    @DisplayName("Declining granted cumulative upkeep sacrifices the enchantment")
    void decliningGrantedCumulativeUpkeepSacrifices() {
        Permanent shaman = addReady(player1, new BalduvianShaman());
        Permanent cop = harness.addToBattlefieldAndReturn(player1, new CircleOfProtectionBlack());

        int shamanIdx = gd.playerBattlefields.get(player1.getId()).indexOf(shaman);
        harness.activateAbility(player1, shamanIdx, null, cop.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cop);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Circle of Protection: Black"));
    }

    @Test
    @DisplayName("Cannot target an enchantment that already has cumulative upkeep")
    void cannotTargetEnchantmentWithCumulativeUpkeep() {
        Permanent shaman = addReady(player1, new BalduvianShaman());
        Permanent storm = harness.addToBattlefieldAndReturn(player1, new EnergyStorm());

        int shamanIdx = gd.playerBattlefields.get(player1.getId()).indexOf(shaman);
        assertThatThrownBy(() -> harness.activateAbility(player1, shamanIdx, null, storm.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot retarget the same enchantment after granting cumulative upkeep")
    void cannotRetargetAfterGrant() {
        Permanent shaman = addReady(player1, new BalduvianShaman());
        Permanent cop = harness.addToBattlefieldAndReturn(player1, new CircleOfProtectionBlack());

        int shamanIdx = gd.playerBattlefields.get(player1.getId()).indexOf(shaman);
        harness.activateAbility(player1, shamanIdx, null, cop.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "BLUE");

        shaman.untap();
        shaman.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, shamanIdx, null, cop.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target opponent's white enchantment")
    void cannotTargetOpponentsEnchantment() {
        Permanent shaman = addReady(player1, new BalduvianShaman());
        Permanent cop = harness.addToBattlefieldAndReturn(player2, new CircleOfProtectionBlack());

        int shamanIdx = gd.playerBattlefields.get(player1.getId()).indexOf(shaman);
        assertThatThrownBy(() -> harness.activateAbility(player1, shamanIdx, null, cop.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
