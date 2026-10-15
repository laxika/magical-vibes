package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CoralhelmCommander;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeeprootHistorian.class, CoralhelmCommander.class, Forest.class, GrizzlyBears.class, LlanowarElves.class, MaskwoodNexus.class})
class DeeprootHistorianTest extends BaseCardTest {

    @Test
    @DisplayName("Grants retrace to Merfolk cards in your graveyard")
    void grantsRetraceToMerfolkCards() {
        harness.addToBattlefield(player1, new DeeprootHistorian());
        harness.setGraveyard(player1, List.of(new CoralhelmCommander()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Coralhelm Commander");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Does not grant retrace to cards without a Merfolk or Druid subtype")
    void doesNotGrantRetraceToOtherCards() {
        harness.addToBattlefield(player1, new DeeprootHistorian());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantsRetraceToDruidsWithoutMerfolkSubtype() {
        harness.addToBattlefield(player1, new DeeprootHistorian());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void doesNotGrantRetraceToOpponentsCards() {
        harness.addToBattlefield(player2, new DeeprootHistorian());
        harness.setGraveyard(player1, List.of(new CoralhelmCommander()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Coralhelm Commander");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void requiresALandDiscard() {
        harness.addToBattlefield(player1, new DeeprootHistorian());
        harness.setGraveyard(player1, List.of(new CoralhelmCommander()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Coralhelm Commander");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    void stillRequiresNormalManaCost() {
        harness.addToBattlefield(player1, new DeeprootHistorian());
        harness.setGraveyard(player1, List.of(new CoralhelmCommander()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Coralhelm Commander");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
    }

    @Test
    void doesNotAllowCreatureCastingDuringUpkeep() {
        harness.addToBattlefield(player1, new DeeprootHistorian());
        harness.setGraveyard(player1, List.of(new CoralhelmCommander()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        gd.currentStep = TurnStep.UPKEEP;

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Coralhelm Commander");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void doesNotGrantRetraceFromItsOwnGraveyard() {
        harness.setGraveyard(player1, List.of(new DeeprootHistorian()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Deeproot Historian");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void canRetraceAnotherHistorianWithOnlyOneLandDiscard() {
        harness.addToBattlefield(player1, new DeeprootHistorian());
        harness.setGraveyard(player1, List.of(new DeeprootHistorian()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Deeproot Historian"))
                .hasSize(2);
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void grantsRetraceToCreatureTypesGrantedByMaskwoodNexus() {
        harness.addToBattlefield(player1, new DeeprootHistorian());
        harness.addToBattlefield(player1, new MaskwoodNexus());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }
}
