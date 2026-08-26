package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodSpatterAnalysis.class, Forest.class, GrizzlyBears.class, Shock.class})
class BloodSpatterAnalysisTest extends BaseCardTest {

    @Test
    void entersAndDealsThreeDamageToTargetCreatureAnOpponentControls() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        BloodSpatterAnalysis analysis = new BloodSpatterAnalysis();
        harness.setHand(player1, List.of(analysis));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentCreature.getId()));
    }

    @Test
    void deathTriggerMillsCountersSacrificesAndChoosesCreatureAfterSacrifice() {
        Permanent analysis = harness.addToBattlefieldAndReturn(player1, new BloodSpatterAnalysis());
        analysis.setCounterCount(CounterType.BLOODSTAIN, 4);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest milledCard = new Forest();
        GrizzlyBears returnTarget = new GrizzlyBears();
        harness.setLibrary(player1, List.of(milledCard));
        harness.setGraveyard(player1, List.of(returnTarget));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(returnTarget.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(analysis.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.handleMultipleCardsChosen(player1, List.of(returnTarget.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(returnTarget.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(returnTarget.getId()));
    }
}
