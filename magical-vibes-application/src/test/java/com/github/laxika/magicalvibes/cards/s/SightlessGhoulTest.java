package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SightlessGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Has can't-block static effect")
    void hasCantBlockEffect() {
        SightlessGhoul card = new SightlessGhoul();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(CantBlockEffect.class);
    }

    @Test
    @DisplayName("Sightless Ghoul cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent ghoul = new Permanent(new SightlessGhoul());
        ghoul.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(ghoul);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Undying returns Sightless Ghoul with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new SightlessGhoul());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, ghoul.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returnedGhoul = findPermanent(player1, "Sightless Ghoul");
        assertThat(returnedGhoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Sightless Ghoul"));
    }

    @Test
    @DisplayName("Undying does not return Sightless Ghoul when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new SightlessGhoul());
        ghoul.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstant(player2, 0, ghoul.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Sightless Ghoul"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Sightless Ghoul"));
        assertThat(gd.stack).isEmpty();
    }
}
