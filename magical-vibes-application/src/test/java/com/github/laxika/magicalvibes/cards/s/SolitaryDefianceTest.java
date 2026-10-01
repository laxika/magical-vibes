package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SolitaryDefiance.class, GrizzlyBears.class, Forest.class, Shock.class})
class SolitaryDefianceTest extends BaseCardTest {

    @Test
    void soleCreatureGetsVigilanceAndWard() {
        harness.addToBattlefield(player1, new SolitaryDefiance());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.WARD)).isTrue();
    }

    @Test
    void staticAbilitiesTurnOffWithTwoCreatures() {
        harness.addToBattlefield(player1, new SolitaryDefiance());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.WARD)).isFalse();
    }

    @Test
    void attackingAloneSeeksTwoNonlandsThenDiscards() {
        harness.addToBattlefield(player1, new SolitaryDefiance());
        addCreatureReady(player1, new GrizzlyBears());
        Card toDiscard = new Shock();
        Card soughtCreature = new GrizzlyBears();
        Card soughtSpell = new Shock();
        harness.setHand(player1, List.of(toDiscard));
        harness.setLibrary(player1, List.of(new Forest(), soughtCreature, soughtSpell));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(toDiscard, soughtCreature, soughtSpell);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(soughtCreature, soughtSpell);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(toDiscard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multipleAttackersDoNotTriggerSeek() {
        harness.addToBattlefield(player1, new SolitaryDefiance());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Shock()));

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void wardCountersSpellUnlessOpponentPaysTwo() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SolitaryDefiance());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }
}
