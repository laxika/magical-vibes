package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RekiTheHistoryOfKamigawa;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThePeregrineDynamo.class, RekiTheHistoryOfKamigawa.class,
        TheOzolith.class, ProdigalPyromancer.class, GrizzlyBears.class})
class ThePeregrineDynamoTest extends BaseCardTest {

    @Test
    void copiesAnAbilityFromAnotherLegendaryNoncommanderSource() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new RekiTheHistoryOfKamigawa());
        harness.addToBattlefield(player1, new ThePeregrineDynamo());
        harness.setHand(player1, List.of(new TheOzolith()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        UUID rekiTriggerId = gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow()
                .getCard()
                .getId();
        harness.activateAbility(player1, 1, null, rekiTriggerId);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void cannotTargetAnAbilityFromANonlegendarySource() {
        harness.addToBattlefield(player1, new ThePeregrineDynamo());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, player2.getId());
        UUID pyromancerAbilityId = gd.stack.getLast().getCard().getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, pyromancerAbilityId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetAnAbilityFromACommanderSource() {
        Permanent reki = harness.addToBattlefieldAndReturn(player1, new RekiTheHistoryOfKamigawa());
        reki.setCommander(true);
        harness.addToBattlefield(player1, new ThePeregrineDynamo());
        harness.setHand(player1, List.of(new TheOzolith()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        UUID rekiTriggerId = gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow()
                .getCard()
                .getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, rekiTriggerId))
                .isInstanceOf(IllegalStateException.class);
    }
}
