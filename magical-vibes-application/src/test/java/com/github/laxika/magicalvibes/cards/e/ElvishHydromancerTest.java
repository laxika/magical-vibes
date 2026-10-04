package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvishHydromancer.class, GrizzlyBears.class, FountainOfYouth.class})
class ElvishHydromancerTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotCreateToken() {
        harness.castFromHand(player1, new ElvishHydromancer(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void kickedCreatesTokenCopyOfTargetCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ElvishHydromancer()));
        addKickedMana();

        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(gqs.getEffectivePower(gd, target));
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(gqs.getEffectiveToughness(gd, target));
    }

    @Test
    void kickedCannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ElvishHydromancer()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedCannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ElvishHydromancer()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedOnEmptyBattlefieldCanCopyItselfWithoutRetriggering() {
        harness.setHand(player1, List.of(new ElvishHydromancer()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        Permanent hydromancer = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handlePermanentChosen(player1, hydromancer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void copyDoesNotInheritCountersOrTappedState() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ElvishHydromancer());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.tap();
        harness.setHand(player1, List.of(new ElvishHydromancer()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(token.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(gqs.getEffectivePower(gd, target) - 2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(gqs.getEffectiveToughness(gd, target) - 2);
    }

    @Test
    void targetLeavingBeforeAbilityResolvesDoesNotPreventHydromancerEntering() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ElvishHydromancer());
        harness.setHand(player1, List.of(new ElvishHydromancer()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
