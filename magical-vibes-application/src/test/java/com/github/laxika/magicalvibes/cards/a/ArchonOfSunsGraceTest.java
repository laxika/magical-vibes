package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DaxosBlessedByTheSun;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Ichthyomorphosis;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchonOfSunsGrace.class, GloriousAnthem.class, GrizzlyBears.class, DaxosBlessedByTheSun.class, Ichthyomorphosis.class})
class ArchonOfSunsGraceTest extends BaseCardTest {

    @Test
    @DisplayName("An enchantment entering under your control creates a flying Pegasus with lifelink")
    void enchantmentCreatesLifelinkingPegasus() {
        addCreatureReady(player1, new ArchonOfSunsGrace());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent pegasus = findPermanent(player1, "Pegasus");

        assertThat(pegasus).isNotNull();
        assertThat(gqs.getEffectivePower(gd, pegasus)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, pegasus, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, pegasus, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("An enchantment controlled by an opponent does not trigger the Archon")
    void opponentsEnchantmentDoesNotCreatePegasus() {
        addCreatureReady(player1, new ArchonOfSunsGrace());
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Pegasus")).isZero();
    }

    @Test
    @DisplayName("An enchantment creature entering without being cast triggers every Archon")
    void enchantmentCreatureTriggersEachArchon() {
        addCreatureReady(player1, new ArchonOfSunsGrace());
        addCreatureReady(player1, new ArchonOfSunsGrace());

        harness.enterBattlefieldAndReturn(player1, new DaxosBlessedByTheSun());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Pegasus")).isEqualTo(2);
        for (Permanent pegasus : findPermanents(player1, "Pegasus")) {
            assertThat(gqs.getEffectivePower(gd, pegasus)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, pegasus)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, pegasus, Keyword.FLYING)).isTrue();
            assertThat(gqs.hasKeyword(gd, pegasus, Keyword.LIFELINK)).isTrue();
        }
    }

    @Test
    @DisplayName("A pending constellation trigger survives the Archon leaving, but the token has no lifelink")
    void pendingTriggerSurvivesSourceRemoval() {
        Permanent archon = addCreatureReady(player1, new ArchonOfSunsGrace());
        harness.enterBattlefieldAndReturn(player1, new DaxosBlessedByTheSun());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, archon));

        resolveAllTriggers();

        Permanent pegasus = findPermanent(player1, "Pegasus");
        assertThat(countPermanents(player1, "Pegasus")).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, pegasus, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, pegasus, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Pegasus tokens lose lifelink when the last Archon leaves")
    void lifelinkEndsWhenLastArchonLeaves() {
        Permanent first = addCreatureReady(player1, new ArchonOfSunsGrace());
        Permanent second = addCreatureReady(player1, new ArchonOfSunsGrace());
        harness.enterBattlefieldAndReturn(player1, new DaxosBlessedByTheSun());
        resolveAllTriggers();
        Permanent pegasus = findPermanent(player1, "Pegasus");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, first));
        assertThat(gqs.hasKeyword(gd, pegasus, Keyword.LIFELINK)).isTrue();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, second));
        assertThat(gqs.hasKeyword(gd, pegasus, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("An Archon with no abilities does not trigger constellation")
    void losingAbilitiesStopsConstellation() {
        Permanent archon = addCreatureReady(player1, new ArchonOfSunsGrace());
        harness.setHand(player2, List.of(new Ichthyomorphosis()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0, archon.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, archon, Keyword.FLYING)).isFalse();
        harness.enterBattlefieldAndReturn(player1, new DaxosBlessedByTheSun());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Pegasus")).isZero();
    }

    @Test
    @DisplayName("Multiple Archons do not multiply life gained from one Pegasus dealing damage")
    void multipleLifelinkGrantsGainLifeOnlyOnce() {
        addCreatureReady(player1, new ArchonOfSunsGrace());
        addCreatureReady(player1, new ArchonOfSunsGrace());
        harness.enterBattlefieldAndReturn(player1, new DaxosBlessedByTheSun());
        resolveAllTriggers();
        Permanent pegasus = findPermanent(player1, "Pegasus");
        pegasus.setSummoningSick(false);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(pegasus)));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Your Archon does not grant lifelink to an opponent's Pegasus")
    void opponentsPegasusDoesNotGainLifelink() {
        Permanent opponentArchon = addCreatureReady(player2, new ArchonOfSunsGrace());
        harness.enterBattlefieldAndReturn(player2, new DaxosBlessedByTheSun());
        resolveAllTriggers();
        Permanent pegasus = findPermanent(player2, "Pegasus");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, opponentArchon));
        addCreatureReady(player1, new ArchonOfSunsGrace());

        assertThat(gqs.hasKeyword(gd, pegasus, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, pegasus, Keyword.FLYING)).isTrue();
    }
}
