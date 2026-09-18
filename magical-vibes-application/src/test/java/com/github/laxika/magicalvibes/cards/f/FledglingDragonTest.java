package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BorderPatrol;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FledglingDragon.class, BorderPatrol.class})
class FledglingDragonTest extends BaseCardTest {

    @Test
    void remainsBaseSizeBelowThreshold() {
        Permanent dragon = addCreatureReady(player1, new FledglingDragon());
        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));

        assertStats(dragon, 2, 2);
    }

    @Test
    void getsThresholdBoostAtSevenCards() {
        Permanent dragon = addCreatureReady(player1, new FledglingDragon());
        harness.setGraveyard(player1, graveyardWithSevenCards());

        assertStats(dragon, 5, 5);
    }

    @Test
    void thresholdGrantsRedPumpAbility() {
        Permanent dragon = addCreatureReady(player1, new FledglingDragon());
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertStats(dragon, 6, 5);
    }

    @Test
    void redPumpLastsUntilEndOfTurn() {
        Permanent dragon = addCreatureReady(player1, new FledglingDragon());
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertStats(dragon, 6, 5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertStats(dragon, 5, 5);
    }

    @Test
    void opponentGraveyardDoesNotEnableThreshold() {
        Permanent dragon = addCreatureReady(player1, new FledglingDragon());
        harness.setGraveyard(player2, graveyardWithSevenCards());

        assertStats(dragon, 2, 2);
    }

    @Test
    void thresholdPumpAbilityIsUnavailableBelowSevenCards() {
        Permanent dragon = addCreatureReady(player1, new FledglingDragon());
        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Permanent has no activated ability");
    }

    @Test
    void thresholdEffectsEndWhenGraveyardDropsBelowSeven() {
        Permanent dragon = addCreatureReady(player1, new FledglingDragon());
        harness.setGraveyard(player1, graveyardWithSevenCards());
        assertStats(dragon, 5, 5);

        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));

        assertStats(dragon, 2, 2);
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new BorderPatrol(), new BorderPatrol(), new BorderPatrol(), new BorderPatrol(),
                new BorderPatrol(), new BorderPatrol(), new BorderPatrol());
    }

    private void assertStats(Permanent permanent, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(toughness);
    }
}
