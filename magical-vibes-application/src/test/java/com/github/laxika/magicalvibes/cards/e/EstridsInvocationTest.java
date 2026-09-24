package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EstridsInvocation.class, GloriousAnthem.class, GrizzlyBears.class})
class EstridsInvocationTest extends BaseCardTest {

    @Test
    @DisplayName("Copies an enchantment controlled by its controller")
    void copiesAnEnchantmentYouControl() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        EstridsInvocation invocation = castInvocation();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, anthem.getId());

        Permanent copy = findInvocation(invocation);
        assertThat(copy.getCard().getName()).isEqualTo("Glorious Anthem");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not offer an opponent's enchantment as a copy")
    void cannotCopyOpponentEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        EstridsInvocation invocation = castInvocation();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findInvocation(invocation)).isNotNull();
    }

    @Test
    @DisplayName("Its upkeep flicker reoffers the copy choice")
    void flickersAndCopiesAgainAtUpkeep() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        EstridsInvocation invocation = castInvocation();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, anthem.getId());
        Permanent firstCopy = findInvocation(invocation);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, anthem.getId());

        Permanent returnedCopy = findInvocation(invocation);
        assertThat(returnedCopy.getId()).isNotEqualTo(firstCopy.getId());
        assertThat(returnedCopy.getCard().getName()).isEqualTo("Glorious Anthem");
    }

    private EstridsInvocation castInvocation() {
        EstridsInvocation invocation = new EstridsInvocation();
        harness.setHand(player1, List.of(invocation));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        return invocation;
    }

    private Permanent findInvocation(EstridsInvocation invocation) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(invocation.getId()))
                .findFirst()
                .orElseThrow();
    }
}
