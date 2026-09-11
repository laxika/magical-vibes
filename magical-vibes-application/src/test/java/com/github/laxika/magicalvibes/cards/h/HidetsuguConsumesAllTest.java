package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.v.VesselOfTheAllConsuming;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HidetsuguConsumesAll.class, VesselOfTheAllConsuming.class, Memnite.class,
        Mountain.class, GrizzlyBears.class})
class HidetsuguConsumesAllTest extends BaseCardTest {

    @Test
    void chapterIDestroysNonlandPermanentsWithManaValueOneOrLess() {
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        addSagaWithLore(0);

        advanceToNextChapter();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard() instanceof Memnite);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getCard() instanceof Mountain);
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getCard() instanceof GrizzlyBears);
    }

    @Test
    void chapterIIExilesAllGraveyards() {
        harness.setGraveyard(player1, List.of(new Memnite()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        addSagaWithLore(1);

        advanceToNextChapter();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card instanceof Memnite);
        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    void chapterIIITransformsIntoVesselUnderControllerControl() {
        addSagaWithLore(2);

        advanceToNextChapter();

        Permanent vessel = findPermanent(player1, "Vessel of the All-Consuming");
        assertThat(vessel.isTransformed()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof HidetsuguConsumesAll);
    }

    @Test
    void vesselPutsPlusOneCounterOnItWhenItDealsDamage() {
        Permanent vessel = addVessel();
        vessel.setPowerModifier(2);
        vessel.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(vessel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void vesselMakesDamagedPlayerLoseAfterTenDamageThisTurn() {
        Permanent vessel = addVessel();
        vessel.setPowerModifier(7);
        vessel.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new HidetsuguConsumesAll());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent addVessel() {
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new VesselOfTheAllConsuming());
        vessel.setSummoningSick(false);
        return vessel;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
