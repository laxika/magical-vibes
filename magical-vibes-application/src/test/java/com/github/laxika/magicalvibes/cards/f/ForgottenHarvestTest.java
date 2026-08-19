package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ForgottenHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a land puts a +1/+1 counter on a target creature")
    void exilingLandPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new ForgottenHarvest());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Forest forest = new Forest();
        Shock shock = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(forest, shock)));

        triggerUpkeep(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting("id")
                .doesNotContain(forest.getId())
                .contains(shock.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting("id")
                .contains(forest.getId());
    }

    @Test
    @DisplayName("Declining the may ability leaves the graveyard and battlefield unchanged")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new ForgottenHarvest());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Forest forest = new Forest();
        harness.setGraveyard(player1, new ArrayList<>(List.of(forest)));

        triggerUpkeep(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting("id")
                .containsExactly(forest.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The graveyard choice offers only land cards")
    void onlyLandCardsAreOffered() {
        harness.addToBattlefield(player1, new ForgottenHarvest());
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        Shock shock = new Shock();
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, new ArrayList<>(List.of(firstForest, shock, secondForest)));

        triggerUpkeep(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(firstForest.getId(), secondForest.getId());
    }

    @Test
    @DisplayName("Only creatures can be chosen for the counter")
    void onlyCreaturesCanBeTargeted() {
        harness.addToBattlefield(player1, new ForgottenHarvest());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent millstone = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Forest forest = new Forest();
        harness.setGraveyard(player1, new ArrayList<>(List.of(forest)));

        triggerUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(creature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, millstone.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void triggerUpkeep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
