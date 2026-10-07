package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NivixGuildmage;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpectrumSentinel.class, Forest.class, GhostQuarter.class, GrizzlyBears.class, NivixGuildmage.class, TurnToFrog.class})
class SpectrumSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Has protection from multicolored sources")
    void hasProtectionFromMulticoloredSources() {
        Permanent sentinel = addCreatureReady(player1, new SpectrumSentinel());
        Permanent multicoloredSource = addCreatureReady(player2, new NivixGuildmage());

        assertThat(gqs.hasProtectionFromSource(gd, sentinel, multicoloredSource)).isTrue();
    }

    @Test
    @DisplayName("Is not protected from monocolored sources")
    void isNotProtectedFromMonocoloredSources() {
        Permanent sentinel = addCreatureReady(player1, new SpectrumSentinel());
        Permanent monocoloredSource = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasProtectionFromSource(gd, sentinel, monocoloredSource)).isFalse();
    }

    @Test
    @DisplayName("Gains 1 life when an opponent's nonbasic land enters")
    void gainsLifeForOpponentNonbasicLand() {
        harness.addToBattlefield(player1, new SpectrumSentinel());
        harness.setLife(player1, 20);
        playLand(player2, new GhostQuarter());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's basic land")
    void doesNotTriggerForOpponentBasicLand() {
        harness.addToBattlefield(player1, new SpectrumSentinel());
        harness.setLife(player1, 20);
        playLand(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger for the controller's nonbasic land")
    void doesNotTriggerForControllerNonbasicLand() {
        harness.addToBattlefield(player1, new SpectrumSentinel());
        harness.setLife(player1, 20);
        playLand(player1, new GhostQuarter());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot be blocked by a multicolored creature")
    void cannotBeBlockedByMulticoloredCreature() {
        addCreatureReady(player1, new SpectrumSentinel());
        addCreatureReady(player2, new NivixGuildmage());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Prevents combat damage from a multicolored creature it blocks")
    void preventsCombatDamageFromMulticoloredAttacker() {
        addCreatureReady(player1, new NivixGuildmage());
        Permanent sentinel = addCreatureReady(player2, new SpectrumSentinel());
        harness.setLife(player2, 20);
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(sentinel);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each Sentinel gains life independently for an opponent's nonbasic land")
    void multipleSentinelsTriggerIndependently() {
        harness.addToBattlefield(player1, new SpectrumSentinel());
        harness.addToBattlefield(player1, new SpectrumSentinel());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        playLand(player2, new GhostQuarter());

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A queued life-gain trigger resolves after its Sentinel leaves")
    void lifeGainResolvesAfterSourceLeaves() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new SpectrumSentinel());
        harness.setLife(player1, 20);
        playLand(player2, new GhostQuarter());
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(sentinel);
        gd.playerGraveyards.get(player1.getId()).add(sentinel.getCard());

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not gain life for a land entering while its abilities are removed")
    void doesNotTriggerWhileAbilitiesAreRemoved() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new SpectrumSentinel());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, sentinel.getId());

        playLand(player2, new GhostQuarter());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private void playLand(Player player, Card land) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player, land, "");
    }
}
