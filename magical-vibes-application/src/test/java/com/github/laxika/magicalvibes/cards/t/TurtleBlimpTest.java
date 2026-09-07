package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TurtleBlimp.class, GrizzlyBears.class})
class TurtleBlimpTest extends BaseCardTest {

    @Test
    void createsMutantTokenWhenItEnters() {
        harness.setHand(player1, List.of(new TurtleBlimp()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Mutant").getFirst();
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.MUTANT);
    }

    @Test
    void crewAnimatesBlimpUntilEndOfTurn() {
        Permanent blimp = addReady(new TurtleBlimp());
        Permanent crewer = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, blimp)).isTrue();
        assertThat(crewer.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, blimp)).isFalse();
    }

    private Permanent addReady(TurtleBlimp blimp) {
        Permanent permanent = new Permanent(blimp);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
