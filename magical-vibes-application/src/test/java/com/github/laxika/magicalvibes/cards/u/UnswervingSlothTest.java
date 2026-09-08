package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UnswervingSlothTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking while saddled grants indestructible and untaps your creatures")
    void attacksWhileSaddled() {
        Permanent sloth = addCreatureReady(player1, new UnswervingSloth());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        sloth.setSaddled(true);
        ally.tap();
        opponent.tap();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(sloth.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
        assertThat(opponent.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, sloth, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sloth, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Attacking while not saddled does not trigger")
    void doesNotTriggerWhenNotSaddled() {
        Permanent sloth = addCreatureReady(player1, new UnswervingSloth());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        ally.tap();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(sloth.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, sloth, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The trigger checks saddled when attackers are declared")
    void checksSaddledAtDeclaration() {
        Permanent sloth = addCreatureReady(player1, new UnswervingSloth());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        ally.tap();

        declareAttackers(player1, List.of(0));
        sloth.setSaddled(true);
        resolveAllTriggers();

        assertThat(sloth.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, sloth, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
