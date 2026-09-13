package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.t.TandemTactics;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DackFayden.class, Forest.class, GiantGrowth.class, GoldMyr.class, GrizzlyBears.class, LeoninScimitar.class, TandemTactics.class})
class DackFaydenTest extends BaseCardTest {

    @Test
    @DisplayName("+1 makes a target player draw two cards, then discard two cards")
    void plusOneDrawsAndDiscardsForTargetPlayer() {
        Permanent dack = addReadyDack(player1, 3);
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId()))
                .hasSize(2)
                .allMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .hasSize(2)
                .allMatch(card -> card instanceof Forest);
        assertThat(dack.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-2 permanently gains control of a target artifact")
    void minusTwoGainsControlOfArtifact() {
        Permanent dack = addReadyDack(player1, 5);
        Permanent scimitar = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        harness.activateAbility(player1, 0, 1, null, scimitar.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(scimitar.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .doesNotContain(scimitar.getId());
        assertThat(dack.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("-6 gains control of every permanent targeted by a spell")
    void ultimateGainsControlOfAllSpellTargets() {
        Permanent dack = addReadyDack(player1, 6);
        Permanent firstBear = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new TandemTactics()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, List.of(firstBear.getId(), secondBear.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .containsExactlyInAnyOrder(firstBear.getId(), secondBear.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(dack.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    private Permanent addReadyDack(Player player, int loyalty) {
        Permanent perm = new Permanent(new DackFayden());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
    @Test
    @DisplayName("+1 draws two cards, then makes the target player discard two cards")
    void plusOneDrawsAndDiscards() {
        Permanent dack = addReadyDack(player1, 3);
        harness.setHand(player2, List.of(new DackFayden(), new DackFayden()));
        harness.setLibrary(player2, List.of(new DackFayden(), new DackFayden()));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(dack.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-2 gains permanent control of target artifact")
    void minusTwoGainsControlOfCreatureArtifact() {
        Permanent dack = addReadyDack(player1, 3);
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new GoldMyr());

        harness.activateAbility(player1, 0, 1, null, myr.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentController(gd, myr.getId())).isEqualTo(player1.getId());
        assertThat(dack.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-6 creates an emblem that gains control of permanents targeted by your spells")
    void minusSixCreatesTargetedPermanentControlEmblem() {
        Permanent dack = addReadyDack(player1, 6);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).singleElement().satisfies(emblem -> {
            assertThat(emblem.controllerId()).isEqualTo(player1.getId());
            assertThat(emblem.staticEffects()).singleElement()
                    .isInstanceOf(SpellCastTriggerEffect.class);
        });
        assertThat(dack.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
    }

    @Test
    @DisplayName("Emblem gains control of every permanent targeted by a spell")
    void emblemGainsControlOfTargetedPermanent() {
        gd.emblems.add(new Emblem(player1.getId(),
                List.of(new SpellCastTriggerEffect(null, List.of(new GainControlOfTargetEffect(ControlDuration.PERMANENT)), new StackEntryTargetsPermanentPredicate(new PermanentTruePredicate()))),
                new DackFayden()));
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new GoldMyr());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, myr.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gqs.findPermanentController(gd, myr.getId())).isEqualTo(player1.getId());
    }

}
