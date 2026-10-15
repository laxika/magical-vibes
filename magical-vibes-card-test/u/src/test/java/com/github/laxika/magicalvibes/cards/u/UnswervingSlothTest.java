package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnswervingSloth.class, BrightfieldGlider.class})
class UnswervingSlothTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking while saddled grants indestructible and untaps your creatures")
    void attacksWhileSaddled() {
        Permanent sloth = addCreatureReady(player1, new UnswervingSloth());
        Permanent ally = addCreatureReady(player1, new BrightfieldGlider());
        Permanent opponent = addCreatureReady(player2, new BrightfieldGlider());
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
        Permanent ally = addCreatureReady(player1, new BrightfieldGlider());
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
        Permanent ally = addCreatureReady(player1, new BrightfieldGlider());
        ally.tap();

        declareAttackers(player1, List.of(0));
        sloth.setSaddled(true);
        resolveAllTriggers();

        assertThat(sloth.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, sloth, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Saddle taps another creature and enables the attack ability")
    void saddlesThroughItsOwnAbility() {
        Permanent sloth = addCreatureReady(player1, new UnswervingSloth());
        Permanent rider = addCreatureReady(player1, new UnswervingSloth());
        rider.setSummoningSick(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(sloth.isSaddled()).isTrue();
        assertThat(sloth.isTapped()).isFalse();
        assertThat(rider.isTapped()).isTrue();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(sloth.isTapped()).isFalse();
        assertThat(rider.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, sloth, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, rider, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger still untaps allies after Sloth leaves the battlefield")
    void untapsAlliesWhenSourceLeaves() {
        Permanent sloth = addCreatureReady(player1, new UnswervingSloth());
        Permanent ally = addCreatureReady(player1, new BrightfieldGlider());
        sloth.setSaddled(true);
        ally.tap();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).isNotEmpty();
        gd.playerBattlefields.get(player1.getId()).remove(sloth);
        gd.playerGraveyards.get(player1.getId()).add(sloth.getCard());
        resolveAllTriggers();

        assertThat(ally.isTapped()).isFalse();
    }
}
