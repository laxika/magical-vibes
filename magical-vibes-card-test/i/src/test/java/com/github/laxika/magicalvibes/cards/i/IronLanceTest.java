package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IronLance.class, FreshVolunteers.class, Forest.class, Disenchant.class})
class IronLanceTest extends BaseCardTest {

    @Test
    @DisplayName("A newly entered Iron Lance can target its controller's creature during the opponent's turn")
    void newlyEnteredLanceCanActivateDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new IronLance());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Destroying Iron Lance in response does not prevent its ability from resolving")
    void abilityResolvesAfterSourceIsDestroyed() {
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new IronLance());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player2, 0, lance.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lance);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Target creature gains first strike until end of turn")
    void targetCreatureGainsFirstStrike() {
        Permanent lance = addCreatureReady(player1, new IronLance());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(lance.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new IronLance());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new IronLance());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without paying the {3} cost")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new IronLance());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate while Iron Lance is tapped")
    void cannotActivateWhenTapped() {
        Permanent lance = addCreatureReady(player1, new IronLance());
        lance.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
