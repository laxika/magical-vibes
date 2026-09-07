package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({HarvesterOfMisery.class, GrizzlyBears.class, HillGiant.class})
class HarvesterOfMiseryTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives other creatures -2/-2 and leaves Harvester of Misery alone")
    void etbDebuffsOtherCreatures() {
        Permanent ownGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castHarvester();
        resolveHarvesterAndItsTrigger();

        assertThat(ownGiant.getPowerModifier()).isEqualTo(-2);
        assertThat(ownGiant.getToughnessModifier()).isEqualTo(-2);
        assertThat(opposingGiant.getPowerModifier()).isEqualTo(-2);
        assertThat(opposingGiant.getToughnessModifier()).isEqualTo(-2);
        harness.assertOnBattlefield(player1, "Harvester of Misery");
    }

    @Test
    @DisplayName("ETB puts other creatures with two toughness into their owners' graveyards")
    void etbKillsOtherSmallCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castHarvester();
        resolveHarvesterAndItsTrigger();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Harvester of Misery");
    }

    @Test
    @DisplayName("Discarding this card gives a target creature -2/-2 until end of turn")
    void discardAbilityDebuffsTargetCreature() {
        harness.setHand(player1, List.of(new HarvesterOfMisery()));
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isEqualTo(-2);
        assertThat(giant.getToughnessModifier()).isEqualTo(-2);
        harness.assertInGraveyard(player1, "Harvester of Misery");
    }

    @Test
    @DisplayName("The discard ability's debuff wears off at end of turn")
    void discardAbilityDebuffWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new HarvesterOfMisery()));
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, giant.getId());
        harness.passBothPriorities();
        assertThat(giant.getPowerModifier()).isEqualTo(-2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isEqualTo(0);
        assertThat(giant.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The discard ability cannot target a player")
    void discardAbilityRejectsPlayerTarget() {
        harness.setHand(player1, List.of(new HarvesterOfMisery()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Harvester of Misery");
    }

    private void castHarvester() {
        harness.setHand(player1, List.of(new HarvesterOfMisery()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveHarvesterAndItsTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
