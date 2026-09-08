package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.t.TheIncredibleHulk;
import com.github.laxika.magicalvibes.model.CounterType;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BruceBanner.class, TheIncredibleHulk.class, GrizzlyBears.class, LightningStrike.class})
class BruceBannerTest extends BaseCardTest {

    @Test
    @DisplayName("Draws X cards and taps at sorcery speed")
    void drawsCards() {
        Permanent bruce = addFrontReady(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Grizzly Bears");
        assertThat(bruce.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Transforms Bruce Banner at sorcery speed")
    void transforms() {
        Permanent bruce = addFrontReady(player1);
        prepareMainPhase();
        addHulkMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(bruce.isTransformed()).isTrue();
        assertThat(bruce.getCard()).isInstanceOf(TheIncredibleHulk.class);
    }

    @Test
    @DisplayName("Enrage puts a counter on Hulk when he is dealt damage")
    void enragePutsCounterOnHulk() {
        Permanent hulk = addBackReady(player1);
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, hulk.getId());
        resolveAllTriggers();

        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An attacking Hulk untaps and creates another combat after taking damage")
    void attackingHulkGetsAnotherCombat() {
        Permanent hulk = addBackReady(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 6));
        resolveAllTriggers();

        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hulk.isTapped()).isFalse();
        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);
    }

    private void addHulkMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }

    private Permanent addFrontReady(Player player) {
        return addCreatureReady(player, new BruceBanner());
    }

    private Permanent addBackReady(Player player) {
        BruceBanner card = new BruceBanner();
        Permanent permanent = addCreatureReady(player, card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
