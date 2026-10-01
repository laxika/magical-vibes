package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.cards.c.CacklingFlames;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BraceForImpact.class, AssaultZeppelid.class, CacklingFlames.class, MistralCharger.class})
class BraceForImpactTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage to a multicolored creature and adds a counter for each damage prevented")
    void preventsAllDamageAndAddsCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AssaultZeppelid());

        castBraceForImpact(target);
        castDamage(target);
        castDamage(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Can target a multicolored creature controlled by an opponent")
    void canTargetOpponentsMulticoloredCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());

        castBraceForImpact(target);
        castDamage(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Damage to another creature is not prevented")
    void onlyTargetedCreatureIsProtected() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AssaultZeppelid());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new AssaultZeppelid());

        castBraceForImpact(target);
        castDamage(otherCreature);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherCreature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Prevention and its counter rider expire at the end of the turn")
    void expiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AssaultZeppelid());

        castBraceForImpact(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        castDamage(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a monocolored creature")
    void cannotTargetMonocoloredCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        harness.setHand(player1, List.of(new BraceForImpact()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castBraceForImpact(Permanent target) {
        harness.setHand(player1, List.of(new BraceForImpact()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void castDamage(Permanent target) {
        harness.setHand(player2, List.of(new CacklingFlames(), new CacklingFlames()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }
}
