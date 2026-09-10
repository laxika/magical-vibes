package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.t.TrialOfZeal;
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

@CardUsed({EchoPerceptiveProdigy.class, ProdigalPyromancer.class, LlanowarElves.class, TrialOfZeal.class})
class EchoPerceptiveProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a target creature's activated ability")
    void copiesCreatureActivatedAbility() {
        harness.setLife(player2, 20);
        addReadyEcho(player1);
        addReadyPyromancer(player1);
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        harness.activateAbility(player1, 1, null, player2.getId());
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
    @DisplayName("Cannot target an ability from a noncreature source")
    void cannotTargetNoncreatureSourceAbility() {
        addReadyEcho(player1);
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
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, triggerId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyEcho(Player player) {
        var echo = harness.addToBattlefieldAndReturn(player, new EchoPerceptiveProdigy());
        echo.setSummoningSick(false);
    }

    private void addReadyPyromancer(Player player) {
        var pyromancer = harness.addToBattlefieldAndReturn(player, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
    }
}
