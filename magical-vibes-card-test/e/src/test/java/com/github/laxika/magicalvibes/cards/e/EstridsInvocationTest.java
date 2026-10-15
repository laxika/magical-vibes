package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EstridsInvocation.class, GloriousAnthem.class, GrizzlyBears.class, Pacifism.class})
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

    @Test
    @DisplayName("Declining the entry copy leaves no upkeep flicker ability")
    void decliningCopyDoesNotGrantUpkeepAbility() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        EstridsInvocation invocation = castInvocation();
        harness.handleMayAbilityChosen(player1, false);
        Permanent original = findInvocation(invocation);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findInvocation(invocation).getId()).isEqualTo(original.getId());
    }

    @Test
    @DisplayName("Declining the upkeep exile keeps the same copied permanent")
    void mayDeclineUpkeepExile() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        EstridsInvocation invocation = castInvocation();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, anthem.getId());
        Permanent original = findInvocation(invocation);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findInvocation(invocation).getId()).isEqualTo(original.getId());
        assertThat(findInvocation(invocation).getCard().getName()).isEqualTo("Glorious Anthem");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining to copy after flickering loses the upkeep ability")
    void mayReturnWithoutCopying() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        EstridsInvocation invocation = castInvocation();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, anthem.getId());
        Permanent original = findInvocation(invocation);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findInvocation(invocation).getId()).isNotEqualTo(original.getId());
        assertThat(findInvocation(invocation).getCard()).isSameAs(invocation);
        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Copying an Aura offers a legal attachment choice before entering")
    void copiesAuraAndChoosesWhatItEnchants() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent pacifism = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        pacifism.setAttachedTo(firstBear.getId());
        EstridsInvocation invocation = castInvocation();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, pacifism.getId());

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, secondBear.getId());
        assertThat(findInvocation(invocation).getAttachedTo()).isEqualTo(secondBear.getId());
        assertThat(pacifism.getAttachedTo()).isEqualTo(firstBear.getId());
    }

    private EstridsInvocation castInvocation() {
        EstridsInvocation invocation = new EstridsInvocation();
        harness.castFromHand(player1, invocation, "{2}{U}");
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
