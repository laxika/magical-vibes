package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RescueSkiff.class, GloriousAnthem.class, GrizzlyBears.class, HolyDay.class})
class RescueSkiffTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureFromGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new RescueSkiff()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void returnsTargetEnchantmentFromGraveyard() {
        Card enchantment = new GloriousAnthem();
        harness.setGraveyard(player1, List.of(enchantment));
        harness.setHand(player1, List.of(new RescueSkiff()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(enchantment.getId()));
        harness.assertNotInGraveyard(player1, "Glorious Anthem");
    }

    @Test
    void cannotTargetNonCreatureNonEnchantmentCard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new RescueSkiff()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(instant.getId()));
        harness.assertInGraveyard(player1, "Holy Day");
    }

    @Test
    void stationUsesTappedCreaturePowerAndUnlocksFlyingAtTenCounters() {
        Permanent skiff = harness.addToBattlefieldAndReturn(player1, new RescueSkiff());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(skiff), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(skiff.getCounterCount(CounterType.CHARGE)).isEqualTo(3);

        skiff.setCounterCount(CounterType.CHARGE, 10);
        assertThat(gqs.isCreature(gd, skiff)).isTrue();
        assertThat(gqs.getEffectivePower(gd, skiff)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, skiff)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, skiff, Keyword.FLYING)).isTrue();
    }

    @Test
    void stationRequiresAnotherUntappedCreature() {
        Permanent skiff = harness.addToBattlefieldAndReturn(player1, new RescueSkiff());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(skiff), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
