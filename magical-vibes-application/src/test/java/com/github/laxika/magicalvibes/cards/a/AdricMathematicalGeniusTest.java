package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AdricMathematicalGenius.class, ProdigalPyromancer.class, Shock.class})
class AdricMathematicalGeniusTest extends BaseCardTest {

    @Test
    @DisplayName("Copies an activated ability you control")
    void copiesActivatedAbilityYouControl() {
        harness.setLife(player2, 20);
        Permanent adric = harness.addToBattlefieldAndReturn(player1, new AdricMathematicalGenius());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        adric.setSummoningSick(false);
        pyromancer.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, null, player2.getId());
        UUID pyromancerAbilityId = gd.stack.getLast().getCard().getId();
        harness.activateAbility(player1, 0, null, pyromancerAbilityId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifices itself to counter an activated or triggered ability")
    void sacrificesItselfToCounterActivatedAbility() {
        harness.setLife(player1, 20);
        Permanent adric = harness.addToBattlefieldAndReturn(player1, new AdricMathematicalGenius());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        adric.setSummoningSick(false);
        pyromancer.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);
        UUID pyromancerAbilityId = gd.stack.getLast().getCard().getId();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, pyromancerAbilityId);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Adric, Mathematical Genius");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ultimate Sacrifice cannot target a spell")
    void ultimateSacrificeCannotTargetSpell() {
        Permanent adric = harness.addToBattlefieldAndReturn(player1, new AdricMathematicalGenius());
        adric.setSummoningSick(false);
        harness.setHand(player2, java.util.List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, gd.stack.getLast().getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
