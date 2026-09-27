package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiveteersConfluence.class, ChandraNalaar.class, Forest.class, GrizzlyBears.class})
class RiveteersConfluenceTest extends BaseCardTest {

    @Test
    void repeatedDrawModeDrawsAndLosesLifeEachTime() {
        harness.setHand(player1, List.of(new RiveteersConfluence()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);

        cast(new int[]{0, 0, 0});
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof GrizzlyBears)
                .hasSize(3);
    }

    @Test
    void damageModeOnlyDamagesOpposingCreaturesAndPlaneswalkers() {
        harness.setHand(player1, List.of(new RiveteersConfluence()));
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opposingPlaneswalker = new Permanent(new ChandraNalaar());
        opposingPlaneswalker.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player2.getId()).add(opposingPlaneswalker);

        cast(new int[]{1, 1, 1});
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(3);
        assertThat(opposingPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void landModePutsAHandOrGraveyardLandOntoTheBattlefieldTapped() {
        Card handLand = new Forest();
        Card graveyardLand = new Forest();
        harness.setHand(player1, List.of(new RiveteersConfluence(), handLand));
        harness.setGraveyard(player1, List.of(graveyardLand));

        cast(new int[]{2, 2, 2});
        harness.passBothPriorities();

        PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(handLand.getId(), graveyardLand.getId());

        harness.handleMultipleCardsChosen(player1, List.of(graveyardLand.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(handLand.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Forest"))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
    }

    private void cast(int[] modeIndices) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelection(3, modeIndices), List.of());
    }
}
