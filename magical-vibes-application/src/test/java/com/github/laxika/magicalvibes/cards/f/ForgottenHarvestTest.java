package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.Abolish;
import com.github.laxika.magicalvibes.cards.d.DivingGriffin;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForgottenHarvest.class, Abolish.class, DivingGriffin.class, RhysticCave.class})
class ForgottenHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a land puts a +1/+1 counter on a target creature")
    void exilingLandPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new ForgottenHarvest());
        Permanent creature = addCreatureReady(player2, new DivingGriffin());
        RhysticCave land = new RhysticCave();
        Abolish nonland = new Abolish();
        harness.setGraveyard(player1, List.of(land, nonland));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting("id")
                .doesNotContain(land.getId())
                .contains(nonland.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting("id")
                .contains(land.getId());
    }

    @Test
    @DisplayName("Declining the may ability leaves the graveyard and battlefield unchanged")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new ForgottenHarvest());
        Permanent creature = addCreatureReady(player2, new DivingGriffin());
        RhysticCave land = new RhysticCave();
        harness.setGraveyard(player1, List.of(land));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting("id")
                .containsExactly(land.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The graveyard choice offers only land cards")
    void onlyLandCardsAreOffered() {
        harness.addToBattlefield(player1, new ForgottenHarvest());
        RhysticCave firstLand = new RhysticCave();
        RhysticCave secondLand = new RhysticCave();
        Abolish nonland = new Abolish();
        Permanent creature = addCreatureReady(player2, new DivingGriffin());
        harness.setGraveyard(player1, List.of(firstLand, nonland, secondLand));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId());
    }

    @Test
    @DisplayName("Only creatures can be chosen for the counter")
    void onlyCreaturesCanBeTargeted() {
        harness.addToBattlefield(player1, new ForgottenHarvest());
        Permanent creature = addCreatureReady(player2, new DivingGriffin());
        Permanent landPermanent = harness.addToBattlefieldAndReturn(player2, new RhysticCave());
        RhysticCave land = new RhysticCave();
        harness.setGraveyard(player1, List.of(land));

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(creature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, landPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Accepting without a land in the graveyard does not put on a counter")
    void acceptingWithoutLandDoesNothing() {
        harness.addToBattlefield(player1, new ForgottenHarvest());
        Permanent creature = addCreatureReady(player2, new DivingGriffin());
        Abolish nonland = new Abolish();
        harness.setGraveyard(player1, List.of(nonland));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting("id")
                .containsExactly(nonland.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The upkeep ability triggers only during its controller's upkeep")
    void triggersOnlyDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new ForgottenHarvest());
        Permanent creature = addCreatureReady(player2, new DivingGriffin());
        RhysticCave land = new RhysticCave();
        harness.setGraveyard(player1, List.of(land));

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting("id")
                .containsExactly(land.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
