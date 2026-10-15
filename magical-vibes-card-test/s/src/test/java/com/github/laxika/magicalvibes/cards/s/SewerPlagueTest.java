package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SewerPlague.class, ShivanHellkite.class, HillGiantHerdgorger.class})
class SewerPlagueTest extends BaseCardTest {

    @Test
    void givesTargetOpponentCreaturePerpetualMinusTwoMinusTwo() {
        Permanent target = addCreatureReady(player2, new ShivanHellkite());

        castSewerPlague(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void perpetuallyLosesOneOneAtTheBeginningOfEachUpkeep() {
        Permanent target = addCreatureReady(player2, new ShivanHellkite());

        castSewerPlague(target);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    void cannotTargetCreatureYouControl() {
        Permanent target = addCreatureReady(player1, new ShivanHellkite());
        harness.setHand(player1, List.of(new SewerPlague()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    void repeatedCastsGrantIndependentUpkeepAbilities() {
        Permanent target = addCreatureReady(player2, new HillGiantHerdgorger());

        castSewerPlague(target);
        castSewerPlague(target);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Hill Giant Herdgorger");
        harness.assertInGraveyard(player2, "Hill Giant Herdgorger");
    }

    @Test
    void perpetualChangesAndUpkeepAbilitySurviveReturningToHandAndRecasting() {
        Permanent target = addCreatureReady(player2, new HillGiantHerdgorger());
        castSewerPlague(target);
        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.setHand(player2, List.of());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, target));
        harness.assertInHand(player2, "Hill Giant Herdgorger");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        Permanent returned = findPermanent(player2, "Hill Giant Herdgorger");
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);
    }

    private void castSewerPlague(Permanent target) {
        harness.setHand(player1, List.of(new SewerPlague()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

}
