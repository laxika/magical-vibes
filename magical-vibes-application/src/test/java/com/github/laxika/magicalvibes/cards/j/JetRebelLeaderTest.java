package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JetRebelLeader.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class JetRebelLeaderTest extends BaseCardTest {

    @Test
    void putsEligibleCreatureTappedAndAttacking() {
        gd.playerAutoStopSteps.put(player1.getId(), EnumSet.of(
                TurnStep.DECLARE_ATTACKERS,
                TurnStep.DECLARE_BLOCKERS));

        addCreatureReady(player1, new JetRebelLeader());
        Card eligibleCreature = new GrizzlyBears();
        Card tooExpensiveCreature = new HillGiant();
        Card nonCreature = new Shock();
        Card otherCard = new Shock();
        Card fifthCard = new Shock();
        harness.setLibrary(player1, List.of(
                tooExpensiveCreature,
                nonCreature,
                eligibleCreature,
                otherCard,
                fifthCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice libraryChoice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(libraryChoice).isNotNull();
        assertThat(libraryChoice.validCardIds()).containsExactly(eligibleCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligibleCreature.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());

        Permanent enteredCreature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == eligibleCreature)
                .findFirst()
                .orElseThrow();
        assertThat(enteredCreature.isTapped()).isTrue();
        assertThat(enteredCreature.isAttacking()).isTrue();
        assertThat(enteredCreature.getAttackTarget()).isEqualTo(player2.getId());
    }
}
