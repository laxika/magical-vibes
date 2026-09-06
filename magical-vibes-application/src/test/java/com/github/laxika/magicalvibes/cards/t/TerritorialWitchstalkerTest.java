package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TerritorialWitchstalker.class, EnormousBaloth.class, GrizzlyBears.class})
class TerritorialWitchstalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger without a creature with power 4 or greater")
    void doesNotTriggerWithoutLargeCreature() {
        Permanent witchstalker = addCreatureReady(player1, new TerritorialWitchstalker());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, witchstalker)).isEqualTo(2);
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Gets +1/+0 and can attack when its controller has a creature with power 4 or greater")
    void getsPumpAndCanAttackWithLargeCreature() {
        Permanent witchstalker = addCreatureReady(player1, new TerritorialWitchstalker());
        addCreatureReady(player1, new EnormousBaloth());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, witchstalker)).isEqualTo(3);
        declareAttackers(List.of(0));

        assertThat(witchstalker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("An opponent's large creature does not satisfy the condition")
    void opponentLargeCreatureDoesNotSatisfyCondition() {
        Permanent witchstalker = addCreatureReady(player1, new TerritorialWitchstalker());
        addCreatureReady(player2, new EnormousBaloth());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, witchstalker)).isEqualTo(2);
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
