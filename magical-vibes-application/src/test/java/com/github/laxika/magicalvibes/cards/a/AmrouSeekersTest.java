package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SilvercoatLion;
import com.github.laxika.magicalvibes.cards.s.SteelWall;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AmrouSeekers.class, GrizzlyBears.class, SilvercoatLion.class, SteelWall.class})
class AmrouSeekersTest extends BaseCardTest {

    @Test
    @DisplayName("Amrou Seekers cannot be blocked by a nonartifact nonwhite creature")
    void cannotBeBlockedByNonartifactNonwhiteCreature() {
        Permanent seekers = attackingSeekers();
        Permanent blocker = addReadyBlocker(new GrizzlyBears());

        prepareBlockerDeclaration();

        assertThatThrownBy(() -> declareBlock(blocker, seekers))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by artifact creatures or white creatures");
    }

    @Test
    @DisplayName("Amrou Seekers can be blocked by a white creature")
    void canBeBlockedByWhiteCreature() {
        Permanent seekers = attackingSeekers();
        Permanent blocker = addReadyBlocker(new SilvercoatLion());

        prepareBlockerDeclaration();
        declareBlock(blocker, seekers);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Amrou Seekers can be blocked by an artifact creature")
    void canBeBlockedByArtifactCreature() {
        Permanent seekers = attackingSeekers();
        Permanent blocker = addReadyBlocker(new SteelWall());

        prepareBlockerDeclaration();
        declareBlock(blocker, seekers);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent attackingSeekers() {
        Permanent seekers = new Permanent(new AmrouSeekers());
        seekers.setSummoningSick(false);
        seekers.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(seekers);
        return seekers;
    }

    private Permanent addReadyBlocker(Card card) {
        Permanent blocker = new Permanent(card);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        return blocker;
    }

    private void prepareBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
