package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SpidersilkNet;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({ElusiveSpellfist.class, LightningBolt.class, GrizzlyBears.class, SpidersilkNet.class})
class ElusiveSpellfistTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell boosts Elusive Spellfist and makes it unblockable")
    void noncreatureSpellBoostsAndMakesUnblockable() {
        Permanent spellfist = addSpellfist();
        int initialPower = gqs.getEffectivePower(gd, spellfist);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gqs.getEffectivePower(gd, spellfist)).isEqualTo(initialPower + 1);
        assertThat(gqs.hasCantBeBlocked(gd, spellfist)).isTrue();
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Elusive Spellfist")
    void creatureSpellDoesNotTrigger() {
        Permanent spellfist = addSpellfist();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, spellfist)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, spellfist)).isFalse();
    }

    @Test
    @DisplayName("The boost and unblockability wear off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        Permanent spellfist = addSpellfist();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gqs.getEffectivePower(gd, spellfist)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, spellfist)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spellfist)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, spellfist)).isFalse();
    }

    @Test
    @DisplayName("Noncreature permanent spells trigger the ability before the spell resolves")
    void artifactSpellTriggersBeforeResolving() {
        Permanent spellfist = addSpellfist();
        harness.setHand(player1, List.of(new SpidersilkNet()));

        harness.castArtifact(player1, 0);
        assertThat(gqs.getEffectivePower(gd, spellfist)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, spellfist)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spellfist)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spellfist)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, spellfist)).isTrue();
        assertThat(countPermanents(player1, "Spidersilk Net")).isZero();

        resolveAllTriggers();
        assertThat(countPermanents(player1, "Spidersilk Net")).isEqualTo(1);
    }

    @Test
    @DisplayName("Each noncreature spell boosts every Spellfist its caster controls")
    void repeatedCastsBoostEachSpellfistIndependently() {
        Permanent first = addSpellfist();
        Permanent second = addCreatureReady(player1, new ElusiveSpellfist());
        Permanent opposing = addCreatureReady(player2, new ElusiveSpellfist());
        harness.setHand(player1, List.of(new SpidersilkNet(), new SpidersilkNet()));

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, first)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, second)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, opposing)).isFalse();
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger Spellfist")
    void opponentSpellDoesNotTrigger() {
        Permanent spellfist = addSpellfist();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, spellfist)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, spellfist)).isFalse();
    }

    @Test
    @DisplayName("The controller's noncreature spell triggers during an opponent's turn")
    void ownSpellDuringOpponentTurnTriggers() {
        Permanent spellfist = addSpellfist();
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, spellfist)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spellfist)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, spellfist)).isTrue();
    }

    @Test
    @DisplayName("A creature spell still gives no boost after the stack resolves")
    void creatureSpellDoesNotBoostAfterResolution() {
        Permanent spellfist = addSpellfist();
        harness.setHand(player1, List.of(new ElusiveSpellfist()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elusive Spellfist")).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, spellfist)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, spellfist)).isFalse();
    }

    @Test
    @DisplayName("A resolved trigger prevents an otherwise legal combat block")
    void triggeredUnblockabilityPreventsBlocking() {
        addSpellfist();
        addCreatureReady(player2, new ElusiveSpellfist());
        harness.setHand(player1, List.of(new SpidersilkNet()));
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSpellfist() {
        Permanent spellfist = addCreatureReady(player1, new ElusiveSpellfist());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return spellfist;
    }
}
