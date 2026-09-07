package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TymaretTheMurderKing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KellanJoinsUp.class, ColossalDreadmaw.class, GrizzlyBears.class, Mountain.class,
        TymaretTheMurderKing.class})
class KellanJoinsUpTest extends BaseCardTest {

    @Test
    void mayExileAndPlotOnlyEligibleCardFromHand() {
        KellanJoinsUp kellan = new KellanJoinsUp();
        GrizzlyBears eligible = new GrizzlyBears();
        Mountain land = new Mountain();
        ColossalDreadmaw tooExpensive = new ColossalDreadmaw();
        harness.setHand(player1, List.of(kellan, eligible, land, tooExpensive));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(eligible);
        assertThat(gd.plottedCardIds).containsExactly(eligible.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(eligible.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(eligible.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land, tooExpensive);
    }

    @Test
    void decliningPlotChoiceLeavesHandUnchanged() {
        KellanJoinsUp kellan = new KellanJoinsUp();
        GrizzlyBears eligible = new GrizzlyBears();
        harness.setHand(player1, List.of(kellan, eligible));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.plottedCardIds).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(eligible);
    }

    @Test
    void legendaryCreatureEntryPutsCountersOnEachControlledCreature() {
        harness.addToBattlefield(player1, new KellanJoinsUp());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TymaretTheMurderKing()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent legendaryCreature = findPermanent(player1, "Tymaret, the Murder King");
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(legendaryCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
