package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({AppaSteadfastGuardian.class, GrizzlyBears.class, Island.class})
class AppaSteadfastGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Airbends any number of other nonland permanents you control")
    void airbendsAnyNumberOfOtherNonlandPermanentsYouControl() {
        Permanent firstBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new AppaSteadfastGuardian()));
        addAppaMana();
        harness.castCreature(player1, 0, List.of(firstBears.getId(), secondBears.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(firstBears.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(secondBears.getOriginalCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("Casting an airbent spell from exile creates an Ally token")
    void castingAirbentSpellFromExileCreatesAllyToken() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new AppaSteadfastGuardian()));
        addAppaMana();
        harness.castCreature(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, bears.getOriginalCard().getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ally")).hasSize(1);
        assertThat(findPermanent(player1, "Ally").getCard().getSubtypes())
                .containsExactly(CardSubtype.ALLY);
    }

    @Test
    @DisplayName("Airbend cannot target a land")
    void airbendCannotTargetLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        harness.setHand(player1, List.of(new AppaSteadfastGuardian()));
        addAppaMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }

    private void addAppaMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
