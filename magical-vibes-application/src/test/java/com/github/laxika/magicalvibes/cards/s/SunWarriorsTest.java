package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunWarriors.class, GrizzlyBears.class})
class SunWarriorsTest extends BaseCardTest {

    @Test
    void firebendingAddsManaEqualToControlledCreatureCountUntilEndOfCombat() {
        addCreatureReady(player1, new SunWarriors());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void activatedAbilityCreatesAnAllyToken() {
        Permanent warriors = addCreatureReady(player1, new SunWarriors());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Ally");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().isToken()).isTrue();
        assertThat(tokens.getFirst().getCard().getSubtypes()).contains(CardSubtype.ALLY);
        assertThat(warriors.isTapped()).isFalse();
    }
}
