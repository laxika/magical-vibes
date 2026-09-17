package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SplinterTheMentor.class, GrizzlyBears.class, RaiseTheAlarm.class})
class SplinterTheMentorTest extends BaseCardTest {

    @Test
    void createsMutagenWhenAnotherNontokenCreatureLeaves() {
        harness.addToBattlefield(player1, new SplinterTheMentor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        removeFromBattlefield(player1, creature);

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void doesNotCreateMutagenWhenAnotherTokenCreatureLeaves() {
        harness.addToBattlefield(player1, new SplinterTheMentor());
        createSoldierTokens(player1);
        Permanent soldier = findPermanents(player1, "Soldier").getFirst();

        removeFromBattlefield(player1, soldier);

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
    }

    @Test
    void createsMutagenWhenSplinterLeaves() {
        Permanent splinter = harness.addToBattlefieldAndReturn(player1, new SplinterTheMentor());

        removeFromBattlefield(player1, splinter);

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    private void createSoldierTokens(Player player) {
        harness.setHand(player, List.of(new RaiseTheAlarm()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.castInstant(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void removeFromBattlefield(Player player, Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
