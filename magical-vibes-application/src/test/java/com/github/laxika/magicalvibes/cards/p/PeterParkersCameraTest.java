package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.t.TrialOfZeal;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PeterParkersCamera.class, ProdigalPyromancer.class, TrialOfZeal.class, LlanowarElves.class})
class PeterParkersCameraTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a target activated ability")
    void copiesActivatedAbility() {
        harness.setLife(player2, 20);
        addCameraWithFilmCounters();
        addReadyPyromancer(player1);
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        int pyromancerIndex = harness.getGameData().playerBattlefields.get(player1.getId()).size() - 1;
        harness.activateAbility(player1, pyromancerIndex, null, player2.getId());
        UUID pyromancerAbilityId = gd.stack.getLast().getCard().getId();

        harness.activateAbility(player1, 0, null, pyromancerAbilityId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, elvesId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Copies a target triggered ability")
    void copiesTriggeredAbility() {
        harness.setLife(player2, 20);
        addCameraWithFilmCounters();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new TrialOfZeal()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        UUID triggerId = gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow()
                .getCard()
                .getId();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, triggerId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Cannot target an opponent's ability")
    void cannotTargetOpponentAbility() {
        harness.addToBattlefield(player1, new PeterParkersCamera());
        addReadyPyromancer(player2);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, gd.stack.getLast().getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyPyromancer(Player player) {
        var permanent = harness.addToBattlefieldAndReturn(player, new ProdigalPyromancer());
        permanent.setSummoningSick(false);
    }

    private void addCameraWithFilmCounters() {
        var camera = harness.addToBattlefieldAndReturn(player1, new PeterParkersCamera());
        camera.setCounterCount(CounterType.FILM, 3);
    }
}
