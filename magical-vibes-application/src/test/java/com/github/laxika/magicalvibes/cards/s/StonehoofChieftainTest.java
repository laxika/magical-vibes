package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StonehoofChieftain.class, GrizzlyBears.class})
class StonehoofChieftainTest extends BaseCardTest {

    @Test
    @DisplayName("Another attacking creature gains trample and indestructible until end of turn")
    void anotherAttackingCreatureGainsKeywords() {
        addCreatureReady(player1, new StonehoofChieftain());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(attacker.getGrantedKeywords())
                .containsExactlyInAnyOrder(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Stonehoof Chieftain does not grant its ability to itself")
    void doesNotGrantKeywordsToItself() {
        Permanent chieftain = addCreatureReady(player1, new StonehoofChieftain());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(chieftain.getGrantedKeywords()).isEmpty();
    }

    @Test
    @DisplayName("The granted keywords wear off at end of turn")
    void grantedKeywordsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new StonehoofChieftain());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        assertThat(attacker.getGrantedKeywords())
                .containsExactlyInAnyOrder(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getGrantedKeywords()).isEmpty();
    }

    @Test
    @DisplayName("Each other attacker gains both keywords while nonattackers do not")
    void grantsKeywordsToEachOtherAttackerOnly() {
        Permanent chieftain = addCreatureReady(player1, new StonehoofChieftain());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(firstAttacker.getGrantedKeywords())
                .containsExactlyInAnyOrder(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE);
        assertThat(secondAttacker.getGrantedKeywords())
                .containsExactlyInAnyOrder(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE);
        assertThat(chieftain.getGrantedKeywords()).isEmpty();
        assertThat(nonattacker.getGrantedKeywords()).isEmpty();
    }

    @Test
    @DisplayName("An opposing attacker does not receive either keyword")
    void doesNotGrantKeywordsToOpposingAttacker() {
        addCreatureReady(player1, new StonehoofChieftain());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getGrantedKeywords()).isEmpty();
    }
}
