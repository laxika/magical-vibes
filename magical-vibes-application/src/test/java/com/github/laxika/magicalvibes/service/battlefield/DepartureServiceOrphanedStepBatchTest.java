package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DepartureServiceOrphanedStepBatchTest {

    private GameData gd;
    private StackEntry entry;
    private UUID permanentId;

    @BeforeEach
    void setUp() {
        UUID playerId = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", playerId, "Player1");
        Card card = new Card();
        card.setName("Step");
        entry = new StackEntry(StackEntryType.SORCERY_SPELL, card, playerId, "Step", List.of());
        permanentId = UUID.randomUUID();
    }

    private DepartureBatch ownedBatch() {
        return DepartureBatch.of(List.of(DepartureIntent.sacrifice(permanentId))).ownedBy(entry.getTargetableId());
    }

    @Test
    @DisplayName("An answered step batch whose entry is neither parked nor on the stack is dropped")
    void dropsAnAnsweredBatchWhoseEntryIsGone() {
        gd.pendingDepartureBatch = ownedBatch();

        DepartureService.discardOrphanedStepBatch(gd);

        assertThat(gd.pendingDepartureBatch).isNull();
    }

    @Test
    @DisplayName("A step batch whose entry is the parked resolution is kept")
    void keepsTheBatchOfTheParkedResolution() {
        DepartureBatch batch = ownedBatch();
        gd.pendingDepartureBatch = batch;
        gd.pendingEffectResolutionEntry = entry;

        DepartureService.discardOrphanedStepBatch(gd);

        assertThat(gd.pendingDepartureBatch).isSameAs(batch);
    }

    @Test
    @DisplayName("A step batch with a question still open is kept")
    void keepsABatchAwaitingAnAnswer() {
        DepartureBatch batch = ownedBatch().awaitingAnswer(permanentId);
        gd.pendingDepartureBatch = batch;

        DepartureService.discardOrphanedStepBatch(gd);

        assertThat(gd.pendingDepartureBatch).isSameAs(batch);
    }

    @Test
    @DisplayName("A batch no resolution step owns is left alone")
    void leavesOtherBatchesAlone() {
        DepartureBatch batch = DepartureBatch.of(List.of(DepartureIntent.sacrifice(permanentId)));
        gd.pendingDepartureBatch = batch;

        DepartureService.discardOrphanedStepBatch(gd);

        assertThat(gd.pendingDepartureBatch).isSameAs(batch);
    }
}
