package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FireNavyTrebuchet.class, GrizzlyBears.class})
@DisplayName("Fire Navy Trebuchet")
class FireNavyTrebuchetTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a tapped and attacking Ballistic Boulder")
    void attackingCreatesBallisticBoulder() {
        addCreatureReady(player1, new FireNavyTrebuchet());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveTokenAttackTargetChoice();

        Permanent boulder = findPermanent(player1, "Ballistic Boulder");
        assertThat(boulder.isTapped()).isTrue();
        assertThat(boulder.isAttacking()).isTrue();
        assertThat(boulder.isAttackedThisTurn()).isTrue();
        assertThat(boulder.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(boulder.getCard().getPower()).isEqualTo(2);
        assertThat(boulder.getCard().getToughness()).isEqualTo(1);
        assertThat(boulder.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(boulder.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("The Ballistic Boulder is sacrificed at the beginning of the next end step")
    void ballisticBoulderIsSacrificedAtNextEndStep() {
        addCreatureReady(player1, new FireNavyTrebuchet());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveTokenAttackTargetChoice();
        assertThat(findPermanent(player1, "Ballistic Boulder")).isNotNull();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ballistic Boulder")).isEmpty();
    }

    private void resolveTokenAttackTargetChoice() {
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
    }
}
