package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.j.JiwariTheEarthAflame;
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

@CardUsed({KitsuneBonesetter.class, JiwariTheEarthAflame.class})
class KitsuneBonesetterTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next 3 damage to a target creature when ahead in hand size")
    void preventsNextThreeDamageWhenControllerHasMoreCards() {
        harness.setHand(player1, List.of(new JiwariTheEarthAflame(), new JiwariTheEarthAflame()));
        harness.setHand(player2, List.of(new JiwariTheEarthAflame()));
        Permanent bonesetter = addCreatureReady(player1, new KitsuneBonesetter());
        Permanent source = addCreatureReady(player1, new JiwariTheEarthAflame());
        Permanent target = addCreatureReady(player2, new JiwariTheEarthAflame());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bonesetter),
                null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isEqualTo(3);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source),
                3, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isZero();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Prevents only the next 3 damage")
    void preventsOnlyNextThreeDamage() {
        harness.setHand(player1, List.of(new JiwariTheEarthAflame(), new JiwariTheEarthAflame()));
        harness.setHand(player2, List.of(new JiwariTheEarthAflame()));
        Permanent bonesetter = addCreatureReady(player1, new KitsuneBonesetter());
        Permanent source = addCreatureReady(player1, new JiwariTheEarthAflame());
        Permanent target = addCreatureReady(player2, new JiwariTheEarthAflame());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bonesetter),
                null, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source),
                4, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Prevention shield expires at the end of the turn")
    void preventionExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new JiwariTheEarthAflame(), new JiwariTheEarthAflame()));
        harness.setHand(player2, List.of(new JiwariTheEarthAflame()));
        Permanent bonesetter = addCreatureReady(player1, new KitsuneBonesetter());
        Permanent target = addCreatureReady(player2, new JiwariTheEarthAflame());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bonesetter),
                null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Cannot activate when hand sizes are tied")
    void cannotActivateWhenHandSizesAreTied() {
        harness.setHand(player1, List.of(new JiwariTheEarthAflame()));
        harness.setHand(player2, List.of(new JiwariTheEarthAflame()));
        Permanent bonesetter = addCreatureReady(player1, new KitsuneBonesetter());
        Permanent target = addCreatureReady(player2, new JiwariTheEarthAflame());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(bonesetter), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when an opponent has more cards")
    void cannotActivateWhenOpponentHasMoreCards() {
        harness.setHand(player1, List.of(new JiwariTheEarthAflame()));
        harness.setHand(player2, List.of(new JiwariTheEarthAflame(), new JiwariTheEarthAflame()));
        Permanent bonesetter = addCreatureReady(player1, new KitsuneBonesetter());
        Permanent target = addCreatureReady(player2, new JiwariTheEarthAflame());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(bonesetter), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new JiwariTheEarthAflame(), new JiwariTheEarthAflame()));
        harness.setHand(player2, List.of(new JiwariTheEarthAflame()));
        Permanent bonesetter = addCreatureReady(player1, new KitsuneBonesetter());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(bonesetter), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
