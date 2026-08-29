package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FolkOfAnHavva;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeastWalkers.class, FolkOfAnHavva.class})
class BeastWalkersTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability puts it on the stack")
    void activatingPutsOnStack() {
        Permanent walkers = addCreatureReady(player1, new BeastWalkers());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(walkers.getId());
    }

    @Test
    @DisplayName("Resolving the ability grants banding until end of turn")
    void resolvingGrantsBanding() {
        Permanent walkers = addCreatureReady(player1, new BeastWalkers());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThat(gqs.hasKeyword(gd, walkers, Keyword.BANDING)).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, walkers, Keyword.BANDING)).isTrue();
    }

    @Test
    @DisplayName("Granted banding wears off at end of turn")
    void bandingWearsOff() {
        Permanent walkers = addCreatureReady(player1, new BeastWalkers());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, walkers, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("Granted banding allows Beast Walkers to attack in a band")
    void grantedBandingAllowsBandWithNonBandingCreature() {
        Permanent walkers = addCreatureReady(player1, new BeastWalkers());
        Permanent folk = addCreatureReady(player1, new FolkOfAnHavva());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1))));

        assertThat(walkers.getBandId()).isNotNull();
        assertThat(walkers.getBandId()).isEqualTo(folk.getBandId());
    }

    @Test
    @DisplayName("Cannot activate the ability without green mana")
    void cannotActivateWithoutGreenMana() {
        addCreatureReady(player1, new BeastWalkers());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activating the ability does not tap Beast Walkers")
    void activatingDoesNotTap() {
        Permanent walkers = addCreatureReady(player1, new BeastWalkers());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(walkers.isTapped()).isFalse();
    }
}
