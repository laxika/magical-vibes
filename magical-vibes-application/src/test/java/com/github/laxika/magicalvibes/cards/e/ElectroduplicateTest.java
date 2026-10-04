package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.s.Stifle;
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

@CardUsed({Electroduplicate.class, BearCub.class, Clone.class, Stifle.class})
class ElectroduplicateTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a hasty token copy of a creature you control")
    void createsHastyTokenCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearCub());
        castElectroduplicate(target.getId());

        Permanent token = token();
        assertThat(token.getCard().getName()).isEqualTo("Bear Cub");
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Sacrifices the token at the beginning of the end step")
    void sacrificesTokenAtEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearCub());
        castElectroduplicate(target.getId());

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearCub());
        harness.setHand(player1, List.of(new Electroduplicate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback creates the token and exiles the spell")
    void flashbackCreatesTokenAndExilesSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearCub());
        harness.setGraveyard(player1, List.of(new Electroduplicate()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, target.getId());

        assertThat(token()).isNotNull();
        harness.assertNotInGraveyard(player1, "Electroduplicate");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Electroduplicate"));
    }

    @Test
    @DisplayName("A creature copying the token inherits its end-step sacrifice ability")
    void copyInheritsSacrificeAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearCub());
        castElectroduplicate(target.getId());
        Permanent originalToken = token();

        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, originalToken.getId());
        Permanent clone = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof Clone)
                .findFirst().orElseThrow();

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(clone.getId())
                        || permanent.getId().equals(originalToken.getId()));
        harness.assertInGraveyard(player1, "Clone");
    }

    @Test
    @DisplayName("Countering the sacrifice trigger does not prevent a trigger on the next end step")
    void sacrificeTriggersAgainAfterBeingCountered() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearCub());
        castElectroduplicate(target.getId());
        Permanent copy = token();
        harness.setHand(player1, List.of(new Stifle()));

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, gd.stack.getLast().getCard().getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(copy);

        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
    }

    @Test
    @DisplayName("The copy does not inherit counters or tapped status")
    void doesNotCopyCountersOrTappedStatus() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearCub());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        target.tap();

        castElectroduplicate(target.getId());

        assertThat(token().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(token().isTapped()).isFalse();
    }

    private void castElectroduplicate(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new Electroduplicate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    private Permanent token() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
