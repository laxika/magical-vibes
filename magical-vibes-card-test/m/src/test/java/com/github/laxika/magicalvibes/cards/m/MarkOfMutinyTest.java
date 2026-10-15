package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarkOfMutiny.class, WalkingCorpse.class, Pacifism.class, Unsummon.class})
class MarkOfMutinyTest extends BaseCardTest {

    private void castAndResolveAt(Permanent target) {
        harness.setHand(player1, List.of(new MarkOfMutiny()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Resolving steals the creature, untaps it, adds a +1/+1 counter and grants haste")
    void resolvesFullEffect() {
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        target.tap();

        castAndResolveAt(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.isTapped()).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Control and haste wear off at cleanup but the +1/+1 counter stays")
    void controlAndHasteExpireCounterRemains() {
        Permanent target = addCreatureReady(player2, new WalkingCorpse());

        castAndResolveAt(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target a creature you already control")
    void canTargetOwnCreature() {
        Permanent own = addCreatureReady(player1, new WalkingCorpse());
        own.tap();

        castAndResolveAt(own);

        assertThat(own.isTapped()).isFalse();
        assertThat(own.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        enchantment.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new MarkOfMutiny()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The stolen creature can attack immediately despite changing controllers")
    void stolenCreatureCanAttackImmediately() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        Permanent target = findPermanent(player2, "Walking Corpse");

        castAndResolveAt(target);
        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Repeated casts add counters and return an owned creature to the same controller")
    void repeatedCastsOnOwnCreatureKeepControlAndCounters() {
        Permanent target = addCreatureReady(player1, new WalkingCorpse());

        castAndResolveAt(target);
        castAndResolveAt(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature returned to hand in response receives none of the effects")
    void targetReturnedToHandBeforeResolution() {
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        target.tap();
        harness.setHand(player1, List.of(new MarkOfMutiny()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Mark of Mutiny");
        harness.assertInHand(player2, "Walking Corpse");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

}
