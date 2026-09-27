package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MatsuTribeBirdstalker;
import com.github.laxika.magicalvibes.cards.o.OboroPalaceInTheClouds;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChargeAcrossTheAraba.class, MatsuTribeBirdstalker.class,
        OboroPalaceInTheClouds.class, Plains.class})
class ChargeAcrossTheArabaTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the chosen Plains and boosts your creatures by their number")
    void returnsChosenPlainsAndBoostsOwnCreatures() {
        Permanent firstCreature = addCreatureReady(player1, new MatsuTribeBirdstalker());
        Permanent secondCreature = addCreatureReady(player1, new MatsuTribeBirdstalker());
        Permanent opposingCreature = addCreatureReady(player2, new MatsuTribeBirdstalker());
        Permanent firstPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent secondPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent opposingPlains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent nonPlainsLand = harness.addToBattlefieldAndReturn(player1, new OboroPalaceInTheClouds());

        harness.castFromHand(player1, new ChargeAcrossTheAraba(), "{4}{W}");
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(firstPlains.getId(), secondPlains.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, List.of(firstPlains.getId(), secondPlains.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nonPlainsLand)
                .doesNotContain(firstPlains, secondPlains);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingPlains);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(firstPlains.getCard(), secondPlains.getCard());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returning no Plains gives no boost and is legal")
    void returningNoPlainsGivesNoBoost() {
        Permanent creature = addCreatureReady(player1, new MatsuTribeBirdstalker());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());

        harness.castFromHand(player1, new ChargeAcrossTheAraba(), "{4}{W}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(plains);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns a controlled Plains to its owner's hand")
    void returnsControlledPlainsToItsOwnersHand() {
        harness.setHand(player2, List.of());
        Plains plainsCard = new Plains();
        plainsCard.setOwnerId(player2.getId());
        Permanent controlledPlains = harness.addToBattlefieldAndReturn(player1, plainsCard);

        harness.castFromHand(player1, new ChargeAcrossTheAraba(), "{4}{W}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(controlledPlains.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(controlledPlains);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(plainsCard);
    }
}
