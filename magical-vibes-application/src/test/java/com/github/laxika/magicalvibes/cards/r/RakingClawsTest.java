package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RakingClaws.class, FountainOfYouth.class, GrizzlyBears.class})
class RakingClawsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Raking Claws grants double strike to target creature")
    void resolvingGrantsDoubleStrike() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RakingClaws()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Double strike granted by Raking Claws wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RakingClaws()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Raking Claws cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new RakingClaws()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cycling Raking Claws discards it and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new RakingClaws()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Raking Claws");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
