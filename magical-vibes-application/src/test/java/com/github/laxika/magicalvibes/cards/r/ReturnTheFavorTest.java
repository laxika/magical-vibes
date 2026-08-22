package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TrialOfZeal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReturnTheFavor.class, ProdigalPyromancer.class, Shock.class, TrialOfZeal.class})
class ReturnTheFavorTest extends BaseCardTest {

    @Test
    @DisplayName("The copy mode copies an activated ability")
    void copiesActivatedAbility() {
        harness.setLife(player2, 20);
        Permanent pyromancer = addReadyPyromancer();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(pyromancer), null, player2.getId());
        UUID abilityId = harness.getGameData().stack.getLast().getCard().getId();
        harness.passPriority(player2);

        cast(new int[]{0}, List.of(abilityId));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The copy mode copies a triggered ability")
    void copiesTriggeredAbility() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new TrialOfZeal()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        UUID triggerId = harness.getGameData().stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow()
                .getCard()
                .getId();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        cast(new int[]{0}, List.of(triggerId));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("The redirect mode changes a single target of a spell")
    void redirectsSingleTargetSpell() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        cast(new int[]{1}, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Both modes can target and resolve in one spell")
    void bothModesResolve() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        cast(new int[]{0, 1}, List.of(shock.getId(), shock.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent addReadyPyromancer() {
        Permanent pyromancer = new Permanent(new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(pyromancer);
        return pyromancer;
    }

    private int battlefieldIndex(Permanent permanent) {
        return harness.getGameData().playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void cast(int[] modes, List<UUID> targets) {
        harness.setHand(player1, List.of(new ReturnTheFavor()));
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targets);
    }
}
