package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AesthirGlider;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvishBard.class, ElvishRanger.class, AesthirGlider.class, Humility.class})
class ElvishBardTest extends BaseCardTest {

    @Test
    @DisplayName("All able creatures must block Elvish Bard")
    void allAbleCreaturesMustBlock() {
        Permanent bard = addCreatureReady(player1, new ElvishBard());
        bard.setAttacking(true);

        addCreatureReady(player2, new ElvishRanger());
        addCreatureReady(player2, new ElvishRanger());

        prepareDeclareBlockers();

        // Only one blocker assigned — should fail because both must block
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        // Both blockers assigned — should succeed
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures are not forced to block Elvish Bard")
    void tappedCreaturesNotForcedToBlock() {
        Permanent bard = addCreatureReady(player1, new ElvishBard());
        bard.setAttacking(true);

        Permanent untapped = addCreatureReady(player2, new ElvishRanger());
        Permanent tapped = addCreatureReady(player2, new ElvishRanger());
        tapped.tap();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(untapped.isBlocking()).isTrue();
        assertThat(tapped.isBlocking()).isFalse();
    }

    @Test
    void noBlockRequiredWhenNoCreatureCanBlock() {
        Permanent bard = addCreatureReady(player1, new ElvishBard());
        bard.setAttacking(true);
        addCreatureReady(player2, new AesthirGlider());

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void noBlockersAreIllegalWhenAbleCreatureExists() {
        Permanent bard = addCreatureReady(player1, new ElvishBard());
        bard.setAttacking(true);
        addCreatureReady(player2, new ElvishRanger());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void losingAllAbilitiesRemovesTheBlockRequirement() {
        Permanent bard = addCreatureReady(player1, new ElvishBard());
        bard.setAttacking(true);
        harness.addToBattlefield(player1, new Humility());
        addCreatureReady(player2, new ElvishRanger());

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

}
