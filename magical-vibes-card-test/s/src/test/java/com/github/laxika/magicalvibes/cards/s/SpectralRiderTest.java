package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.OneEyedScarecrow;
import com.github.laxika.magicalvibes.cards.u.UnrulyMob;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpectralRider.class, OneEyedScarecrow.class, UnrulyMob.class, WalkingCorpse.class})
class SpectralRiderTest extends BaseCardTest {

    @Test
    void nonwhiteNonartifactCreatureCannotBlock() {
        addCreatureReady(player1, new SpectralRider());
        addCreatureReady(player2, new WalkingCorpse());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("intimidate");
    }

    @Test
    void whiteNonartifactCreatureCanBlock() {
        addCreatureReady(player1, new SpectralRider());
        Permanent blocker = addCreatureReady(player2, new UnrulyMob());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void colorlessArtifactCreatureCanBlock() {
        addCreatureReady(player1, new SpectralRider());
        Permanent blocker = addCreatureReady(player2, new OneEyedScarecrow());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void intimidateDoesNotRestrictRidersOwnBlocking() {
        addCreatureReady(player1, new WalkingCorpse());
        Permanent rider = addCreatureReady(player2, new SpectralRider());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(rider.isBlocking()).isTrue();
    }
}
