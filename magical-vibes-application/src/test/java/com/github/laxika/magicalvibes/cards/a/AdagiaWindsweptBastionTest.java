package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(AdagiaWindsweptBastion.class)
class AdagiaWindsweptBastionTest extends BaseCardTest {

    @Test
    @DisplayName("Station adds charge counters equal to another creature's power")
    void stationAddsChargeCountersFromAnotherCreaturePower() {
        Permanent adagia = harness.addToBattlefieldAndReturn(player1, new AdagiaWindsweptBastion());
        Permanent creature = addCreatureReady(player1, creature("Stationing Creature", 3, 3));

        harness.activateAbility(player1, battlefieldIndex(adagia), 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(adagia.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("At twelve charge counters, it copies a controlled artifact as a legendary token")
    void twelveCountersUnlockLegendaryArtifactCopy() {
        Permanent adagia = harness.addToBattlefieldAndReturn(player1, new AdagiaWindsweptBastion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, permanent("Target Artifact", CardType.ARTIFACT));
        adagia.setCounterCount(CounterType.CHARGE, 12);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, battlefieldIndex(adagia), 1, null, artifact.getId());
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getName()).isEqualTo("Target Artifact");
        assertThat(tokens.getFirst().getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(tokens.getFirst().getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("At twelve charge counters, it copies a controlled enchantment")
    void twelveCountersUnlockEnchantmentCopy() {
        Permanent adagia = harness.addToBattlefieldAndReturn(player1, new AdagiaWindsweptBastion());
        Permanent enchantment = harness.addToBattlefieldAndReturn(
                player1, permanent("Target Enchantment", CardType.ENCHANTMENT));
        adagia.setCounterCount(CounterType.CHARGE, 12);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, battlefieldIndex(adagia), 1, null, enchantment.getId());
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(tokens.getFirst().getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("The copy ability requires twelve charge counters and a controlled artifact or enchantment")
    void copyAbilityRejectsInsufficientCountersAndIllegalTargets() {
        Permanent adagia = harness.addToBattlefieldAndReturn(player1, new AdagiaWindsweptBastion());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, permanent("Own Artifact", CardType.ARTIFACT));
        adagia.setCounterCount(CounterType.CHARGE, 11);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(adagia), 1, null, ownArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        adagia.setCounterCount(CounterType.CHARGE, 12);
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(
                player2, permanent("Opponent Artifact", CardType.ARTIFACT));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(adagia), 1, null, opponentArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Card creature(String name, int power, int toughness) {
        Card card = permanent(name, CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private Card permanent(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
