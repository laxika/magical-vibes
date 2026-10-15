package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CharityExtractor;
import com.github.laxika.magicalvibes.cards.t.TrustedPegasus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunbladeAngel.class, CharityExtractor.class, TrustedPegasus.class, Snarespinner.class})
class SunbladeAngelTest extends BaseCardTest {

    @Test
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent angel = addCreatureReady(player1, new SunbladeAngel());
        addCreatureReady(player2, new CharityExtractor());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
        assertThat(angel.isAttacking()).isTrue();
    }

    @Test
    void unblockedAttackGainsLifeOnceAndDoesNotTapAngel() {
        Permanent angel = addCreatureReady(player1, new SunbladeAngel());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(angel.isAttacking()).isTrue();
        assertThat(angel.isTapped()).isFalse();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    void flyingBlockerDiesToFirstStrikeAndLifelinkCountsAllDamage() {
        addCreatureReady(player1, new SunbladeAngel());
        addCreatureReady(player2, new TrustedPegasus());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Sunblade Angel");
        harness.assertInGraveyard(player2, "Trusted Pegasus");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    void reachBlockerDiesBeforeItCanDealLethalDamage() {
        addCreatureReady(player1, new SunbladeAngel());
        addCreatureReady(player2, new Snarespinner());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        resolveCombat();

        harness.assertOnBattlefield(player1, "Sunblade Angel");
        harness.assertInGraveyard(player2, "Snarespinner");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    void canBlockGroundAttackerAndLifelinkBenefitsDefendingController() {
        addCreatureReady(player1, new CharityExtractor());
        addCreatureReady(player2, new SunbladeAngel());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Charity Extractor");
        harness.assertOnBattlefield(player2, "Sunblade Angel");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 23);
    }

    @Test
    void vigilanceDoesNotAllowTappedAngelToAttack() {
        Permanent angel = addCreatureReady(player1, new SunbladeAngel());
        angel.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(angel.isTapped()).isTrue();
        assertThat(angel.isAttacking()).isFalse();
    }
}
