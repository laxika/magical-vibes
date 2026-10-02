package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BiosynthicBurst;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({AdagiaWindsweptBastion.class, AnticausalVestige.class, AllFatesScroll.class,
        BiosynthicBurst.class})
class AdagiaWindsweptBastionTest extends BaseCardTest {

    @Test
    void entersTapped() {
        harness.setHand(player1, List.of(new AdagiaWindsweptBastion()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Adagia, Windswept Bastion").isTapped()).isTrue();
    }

    @Test
    void tapsForWhiteManaWithoutChargeCounters() {
        Permanent adagia = harness.addToBattlefieldAndReturn(player1, new AdagiaWindsweptBastion());

        harness.activateAbility(player1, battlefieldIndex(adagia), 0, null, null);

        assertThat(adagia.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stationWorksWhileLandIsTappedAndCreatureIsSummoningSick() {
        Permanent adagia = harness.addToBattlefieldAndReturn(player1, new AdagiaWindsweptBastion());
        adagia.tap();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AnticausalVestige());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, battlefieldIndex(adagia), 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(adagia.getCounterCount(CounterType.CHARGE)).isEqualTo(7);
    }

    @Test
    void stationUsesPowerWhenItResolves() {
        Permanent adagia = harness.addToBattlefieldAndReturn(player1, new AdagiaWindsweptBastion());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AnticausalVestige());
        harness.setHand(player1, List.of(new BiosynthicBurst()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, battlefieldIndex(adagia), 0, null, null);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(adagia.getCounterCount(CounterType.CHARGE)).isEqualTo(8);
    }

    @Test
    void stationCannotTapAnAlreadyTappedOrOpposingCreature() {
        Permanent adagia = harness.addToBattlefieldAndReturn(player1, new AdagiaWindsweptBastion());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AnticausalVestige());
        creature.tap();
        harness.addToBattlefield(player2, new AnticausalVestige());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(adagia), 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(adagia.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void bothAbilitiesRequireSorceryTiming() {
        Permanent adagia = harness.addToBattlefieldAndReturn(player1, new AdagiaWindsweptBastion());
        harness.addToBattlefield(player1, new AnticausalVestige());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AllFatesScroll());
        adagia.setCounterCount(CounterType.CHARGE, 12);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(adagia), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(adagia), 1, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void copyResolvesAfterChargeCountersAreLostAndRetainsCopiedAbility() {
        Permanent adagia = harness.addToBattlefieldAndReturn(player1, new AdagiaWindsweptBastion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AllFatesScroll());
        adagia.setCounterCount(CounterType.CHARGE, 13);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, battlefieldIndex(adagia), 1, null, artifact.getId());
        assertThat(adagia.isTapped()).isTrue();
        assertThat(adagia.getCounterCount(CounterType.CHARGE)).isEqualTo(13);
        adagia.setCounterCount(CounterType.CHARGE, 0);
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "All-Fates Scroll").stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        harness.activateAbility(player1, battlefieldIndex(token), 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void copyRejectsAnOrdinaryCreature() {
        Permanent adagia = harness.addToBattlefieldAndReturn(player1, new AdagiaWindsweptBastion());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AnticausalVestige());
        adagia.setCounterCount(CounterType.CHARGE, 12);
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(adagia), 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

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
