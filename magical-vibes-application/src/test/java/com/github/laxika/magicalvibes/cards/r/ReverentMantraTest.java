package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReverentMantra.class, FreshVolunteers.class, DeadlyInsect.class, Plains.class})
class ReverentMantraTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures gain protection from the chosen color")
    void grantsProtectionToAllCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        Permanent ownOtherCreature = harness.addToBattlefieldAndReturn(player1, new DeadlyInsect());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new DeadlyInsect());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.castFromHand(player1, new ReverentMantra(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, ownOtherCreature, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opposingCreature, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opposingLand, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, ownOtherCreature, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, opposingCreature, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Can be cast by exiling a white card instead of paying mana")
    void castsWithWhiteCardAlternateCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DeadlyInsect());
        harness.setHand(player1, List.of(new ReverentMantra(), new FreshVolunteers()));

        harness.castInstantWithAlternateExileFromHand(player1, 0, List.of(), 1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName()).containsExactly("Fresh Volunteers");
    }

    @Test
    @DisplayName("Alternate cost requires a white card")
    void alternateCostRequiresWhiteCard() {
        harness.setHand(player1, List.of(new ReverentMantra(), new DeadlyInsect()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player1, 0, List.of(), 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DeadlyInsect());

        harness.castFromHand(player1, new ReverentMantra(), "{3}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isFalse();
    }
}
