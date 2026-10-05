package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KikiJikiMirrorBreaker;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuantumMisalignment.class, KikiJikiMirrorBreaker.class, GrizzlyBears.class})
class QuantumMisalignmentTest extends BaseCardTest {

    @Test
    void createsANonlegendaryTokenCopyOfATargetCreatureYouControl() {
        harness.addToBattlefield(player1, new KikiJikiMirrorBreaker());
        QuantumMisalignment card = new QuantumMisalignment();
        harness.setHand(player1, List.of(card));
        addMana();

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Kiki-Jiki, Mirror Breaker"));

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Kiki-Jiki, Mirror Breaker");
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    void cannotTargetACreatureYouDoNotControl() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuantumMisalignment()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void reboundOffersAFreeSecondCastAtTheNextUpkeep() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        QuantumMisalignment card = new QuantumMisalignment();
        harness.setHand(player1, List.of(card));
        addMana();

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertInGraveyard(player1, "Quantum Misalignment");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void doesNotReboundWhenItsOnlyTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        QuantumMisalignment card = new QuantumMisalignment();
        harness.setHand(player1, List.of(card));
        addMana();

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Quantum Misalignment");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void decliningReboundLeavesTheCardInExileWithoutAnotherOffer() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        QuantumMisalignment card = new QuantumMisalignment();
        harness.setHand(player1, List.of(card));
        addMana();
        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Quantum Misalignment");
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void doesNotCopyCountersOrTappedStatus() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setTapped(true);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new QuantumMisalignment()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
