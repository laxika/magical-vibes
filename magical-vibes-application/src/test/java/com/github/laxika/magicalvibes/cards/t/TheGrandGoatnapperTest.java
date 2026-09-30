package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.Goatnap;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PossessedGoat;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheGrandGoatnapper.class, Goatnap.class, GrizzlyBears.class, PossessedGoat.class})
class TheGrandGoatnapperTest extends BaseCardTest {

    @Test
    void affinityForGoatsReducesSpellCosts() {
        addCreatureReady(player1, new TheGrandGoatnapper());
        addCreatureReady(player1, new PossessedGoat());
        harness.setHand(player1, List.of(new TheGrandGoatnapper()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void turnsAnotherNonGoatCreatureIntoAGoatAndConjuresGoatnap() {
        Permanent source = addCreatureReady(player1, new TheGrandGoatnapper());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.GOAT)).isTrue();
        assertThat(gd.playerHands.get(player1.getId()))
                .anySatisfy(card -> assertThat(card.getName()).isEqualTo("Goatnap"));
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    void cannotTargetGoatOrItself() {
        Permanent source = addCreatureReady(player1, new TheGrandGoatnapper());
        Permanent goat = addCreatureReady(player2, new PossessedGoat());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, goat.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another non-Goat creature");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another non-Goat creature");
    }

    @Test
    void abilityCanOnlyBeActivatedAsASorcery() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new TheGrandGoatnapper());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
