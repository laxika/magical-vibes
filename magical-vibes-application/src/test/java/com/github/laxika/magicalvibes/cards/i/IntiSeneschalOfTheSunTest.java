package com.github.laxika.magicalvibes.cards.i;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

@CardUsed({IntiSeneschalOfTheSun.class, GrizzlyBears.class, Shock.class, Forest.class})
class IntiSeneschalOfTheSunTest extends BaseCardTest {

    @Test
    void discardingOnAttackTargetsAnAttackingCreatureForCounterAndTrample() {
        addCreatureReady(player1, new IntiSeneschalOfTheSun());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Forest discarded = new Forest();
        Shock exiled = new Shock();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(exiled));

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);

        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBeforeCast = gd.getLife(player2.getId());
        harness.castFromExile(player1, exiled.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBeforeCast - 2);
    }

    @Test
    void decliningTheAttackAbilityDoesNotDiscardOrExile() {
        addCreatureReady(player1, new IntiSeneschalOfTheSun());
        Shock discarded = new Shock();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(forest));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }
}
