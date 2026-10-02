package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KithkinGreatheart;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpringjackKnight.class, KithkinGreatheart.class, Forest.class})
class SpringjackKnightTest extends BaseCardTest {

    // ===== Attack trigger: target selection =====

    @Test
    @DisplayName("Attacking queues attack trigger for target selection")
    void attackTriggersTargetSelection() {
        addCreatureReady(player1, new SpringjackKnight());
        addCreatureReady(player1, new KithkinGreatheart());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Attack trigger can target any creature, but not a land")
    void attackTriggerTargetsAnyCreature() {
        addCreatureReady(player1, new SpringjackKnight());
        Permanent ownCreature = addCreatureReady(player1, new KithkinGreatheart());
        Permanent opposingCreature = addCreatureReady(player2, new KithkinGreatheart());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId(), opposingCreature.getId())
                .doesNotContain(opposingLand.getId());
    }

    @Test
    @DisplayName("Choosing target puts the clash trigger on the stack")
    void choosingTargetPutsTriggerOnStack() {
        Permanent knight = addCreatureReady(player1, new SpringjackKnight());
        Permanent ally = addCreatureReady(player1, new KithkinGreatheart());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Springjack Knight")
                        && se.getTargetId().equals(ally.getId())
                        && se.getSourcePermanentId().equals(knight.getId()));
    }

    // ===== Won clash — target gains double strike =====

    @Test
    @DisplayName("Winning the clash grants double strike to the target creature")
    void wonClashGrantsDoubleStrike() {
        // Higher mana value on top for player1 (Kithkin Greatheart MV 2 > Forest MV 0) → player1 wins.
        harness.setLibrary(player1, List.of(new KithkinGreatheart()));
        harness.setLibrary(player2, List.of(new Forest()));

        addCreatureReady(player1, new SpringjackKnight());
        Permanent ally = addCreatureReady(player1, new KithkinGreatheart());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();

        assertThat(ally.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    // ===== Lost clash — no grant =====

    @Test
    @DisplayName("Losing the clash grants nothing")
    void lostClashGrantsNothing() {
        // Lower mana value on top for player1 (Forest MV 0 < Kithkin Greatheart MV 2) → player1 loses.
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new KithkinGreatheart()));

        addCreatureReady(player1, new SpringjackKnight());
        Permanent ally = addCreatureReady(player1, new KithkinGreatheart());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();

        assertThat(ally.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    // ===== Tie — a clash is only won on a strictly greater mana value =====

    @Test
    @DisplayName("An equal mana value tie is not a win, so nothing is granted")
    void tiedClashGrantsNothing() {
        harness.setLibrary(player1, List.of(new KithkinGreatheart()));
        harness.setLibrary(player2, List.of(new KithkinGreatheart()));

        addCreatureReady(player1, new SpringjackKnight());
        Permanent ally = addCreatureReady(player1, new KithkinGreatheart());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();

        assertThat(ally.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    // ===== Cleanup =====

    @Test
    @DisplayName("Double strike wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        harness.setLibrary(player1, List.of(new KithkinGreatheart()));
        harness.setLibrary(player2, List.of(new Forest()));

        addCreatureReady(player1, new SpringjackKnight());
        Permanent ally = addCreatureReady(player1, new KithkinGreatheart());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();
        assertThat(ally.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ally.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

}
