package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LumithreadField.class, BlindPhantasm.class, Opalescence.class})
class LumithreadFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +0/+1")
    void buffsCreaturesYouControl() {
        harness.addToBattlefield(player1, new LumithreadField());
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new BlindPhantasm());

        assertThat(gqs.getEffectivePower(gd, phantasm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, phantasm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not buff creatures controlled by an opponent")
    void doesNotBuffOpponentsCreatures() {
        harness.addToBattlefield(player1, new LumithreadField());
        Permanent phantasm = harness.addToBattlefieldAndReturn(player2, new BlindPhantasm());

        assertThat(gqs.getEffectivePower(gd, phantasm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, phantasm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can be cast face down and turned face up for its morph cost")
    void morphsFaceDownAndTurnsFaceUp() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new BlindPhantasm());
        harness.setHand(player1, List.of(new LumithreadField()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent field = findPermanent(player1, "Lumithread Field");
        assertThat(field.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int fieldIndex = gd.playerBattlefields.get(player1.getId()).indexOf(field);
        harness.turnFaceUp(player1, fieldIndex);
        harness.passBothPriorities();

        assertThat(field.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, phantasm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, phantasm)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Face-down Lumithread Field does not grant its static ability")
    void faceDownFieldDoesNotGrantStaticBonus() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new BlindPhantasm());
        harness.setHand(player1, List.of(new LumithreadField()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Lumithread Field").isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, phantasm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, phantasm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting face up grants the bonus when the enchantment resolves")
    void castsFaceUpAsEnchantment() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new BlindPhantasm());
        harness.castFromHand(player1, new LumithreadField(), "{1}{W}");

        assertThat(gqs.getEffectiveToughness(gd, phantasm)).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, phantasm)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, findPermanent(player1, "Lumithread Field"))).isFalse();
    }

    @Test
    @DisplayName("Multiple fields stack and their bonuses end when they leave")
    void bonusesStackAndEndWhenSourceLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LumithreadField());
        harness.addToBattlefield(player1, new LumithreadField());
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new BlindPhantasm());

        assertThat(gqs.getEffectiveToughness(gd, phantasm)).isEqualTo(5);
        gd.playerBattlefields.get(player1.getId()).remove(first);
        assertThat(gqs.getEffectiveToughness(gd, phantasm)).isEqualTo(4);
    }

    @Test
    @DisplayName("A face-down field receives another field's bonus and stops being a creature immediately when turned face up")
    void faceDownFieldReceivesBonusAndTurnsUpImmediately() {
        harness.addToBattlefield(player1, new LumithreadField());
        harness.setHand(player1, List.of(new LumithreadField()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent field = findPermanents(player1, "Lumithread Field").get(1);

        assertThat(gqs.isCreature(gd, field)).isTrue();
        assertThat(gqs.getEffectivePower(gd, field)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, field)).isEqualTo(3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(field));

        assertThat(field.isFaceDown()).isFalse();
        assertThat(gqs.isCreature(gd, field)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lumithread Field grants its own bonus when Opalescence makes it a creature")
    void animatedFieldReceivesItsOwnBonus() {
        Permanent field = harness.addToBattlefieldAndReturn(player1, new LumithreadField());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, field)).isTrue();
        assertThat(gqs.getEffectivePower(gd, field)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, field)).isEqualTo(3);
    }
}
