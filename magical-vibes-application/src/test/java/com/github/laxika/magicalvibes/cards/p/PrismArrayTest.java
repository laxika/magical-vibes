package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrismArray.class, Forest.class, GrizzlyBears.class})
class PrismArrayTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one crystal counter when only blue mana pays for it")
    void entersWithOneCrystalCounter() {
        Permanent array = castWithMana(ManaColor.BLUE, ManaColor.COLORLESS, ManaColor.COLORLESS,
                ManaColor.COLORLESS, ManaColor.COLORLESS);

        assertThat(array.getCounterCount(CounterType.CRYSTAL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters with one crystal counter for each distinct color spent")
    void entersWithFiveCrystalCounters() {
        Permanent array = castWithMana(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN);

        assertThat(array.getCounterCount(CounterType.CRYSTAL)).isEqualTo(5);
    }

    @Test
    @DisplayName("Removing a crystal counter taps the target creature")
    void removesCounterAndTapsTargetCreature() {
        Permanent array = addArray();
        array.setCounterCount(CounterType.CRYSTAL, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(array.getCounterCount(CounterType.CRYSTAL)).isZero();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The counter-removal ability cannot target a noncreature")
    void counterRemovalCannotTargetNoncreature() {
        Permanent array = addArray();
        array.setCounterCount(CounterType.CRYSTAL, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        assertThat(array.getCounterCount(CounterType.CRYSTAL)).isEqualTo(1);
    }

    @Test
    @DisplayName("The five-color ability scries three cards")
    void scriesThreeCards() {
        Permanent array = addArray();
        Forest first = new Forest();
        GrizzlyBears second = new GrizzlyBears();
        Forest third = new Forest();
        GrizzlyBears fourth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            harness.addMana(player1, color, 1);
        }

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second, third);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, third, fourth);
    }

    private Permanent addArray() {
        return harness.addToBattlefieldAndReturn(player1, new PrismArray());
    }

    private Permanent castWithMana(ManaColor... mana) {
        harness.setHand(player1, List.of(new PrismArray()));
        for (ManaColor color : mana) {
            harness.addMana(player1, color, 1);
        }
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Prism Array");
    }
}
