package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RattleclawMystic.class})
class RattleclawMysticTest extends BaseCardTest {

    @Test
    void tappingPromptsForGreenBlueOrRed() {
        Permanent mystic = addReadyMystic();

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("GREEN", "BLUE", "RED");

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(mystic.isTapped()).isTrue();
    }

    @Test
    void turningFaceUpAddsOneManaOfEachColor() {
        harness.setHand(player1, List.of(new RattleclawMystic()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent mystic = findPermanent(player1, "Rattleclaw Mystic");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mystic));
        harness.passBothPriorities();

        assertThat(mystic.isFaceDown()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"GREEN", "RED"})
    void tappingAddsOnlyTheChosenColorWithoutUsingTheStack(ManaColor color) {
        addReadyMystic();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.stack).isEmpty();
        for (ManaColor candidate : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(candidate))
                    .isEqualTo(candidate == color ? 1 : 0);
        }
    }

    @Test
    void faceUpTriggerWaitsForResolutionEvenWhenMysticIsTappedAndSummoningSick() {
        Permanent mystic = addFaceDownMystic();
        mystic.setSummoningSick(true);
        mystic.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.withAutoStop(gd.currentStep, () -> harness.turnFaceUp(player1, 0));

        assertThat(mystic.isFaceDown()).isFalse();
        assertThat(mystic.isTapped()).isTrue();
        assertThat(mystic.isSummoningSick()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void establishedMysticCanTapForManaWhileItsFaceUpTriggerIsOnTheStack() {
        Permanent mystic = addFaceDownMystic();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.withAutoStop(gd.currentStep, () -> harness.turnFaceUp(player1, 0));

        harness.withAutoStop(gd.currentStep, () -> {
            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, "GREEN");
        });

        assertThat(mystic.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void cannotUseTheFaceUpTriggerManaToPayTheMorphCost() {
        Permanent mystic = addFaceDownMystic();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mystic.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void castingFaceUpDoesNotProduceTheFaceUpTriggerMana() {
        harness.setHand(player1, List.of(new RattleclawMystic()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Rattleclaw Mystic").isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    private Permanent addFaceDownMystic() {
        Permanent mystic = addReadyMystic();
        mystic.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        return mystic;
    }

    private Permanent addReadyMystic() {
        return addCreatureReady(player1, new RattleclawMystic());
    }
}