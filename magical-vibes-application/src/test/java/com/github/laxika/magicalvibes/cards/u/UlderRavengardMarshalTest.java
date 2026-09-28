package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UlderRavengardMarshal.class, GrizzlyBears.class})
class UlderRavengardMarshalTest extends BaseCardTest {

    @Test
    void entersAndGivesAnotherNontokenCreatureDoubleTeamUntilEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ulder = harness.enterBattlefieldAndReturn(player1, new UlderRavengardMarshal());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_TEAM)).isTrue();
        assertThat(gqs.hasKeyword(gd, ulder, Keyword.DOUBLE_TEAM)).isFalse();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    void attackTriggerConjuresDuplicateOfAnotherNontokenAttackingCreature() {
        Permanent ulder = addReadyCreature(new UlderRavengardMarshal());
        Permanent bears = addReadyCreature(new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears")))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    private Permanent addReadyCreature(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
