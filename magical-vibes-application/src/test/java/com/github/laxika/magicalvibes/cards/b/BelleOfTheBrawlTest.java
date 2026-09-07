package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightOfTheKeep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BelleOfTheBrawl.class, KnightOfTheKeep.class, GrizzlyBears.class})
class BelleOfTheBrawlTest extends BaseCardTest {

    @Test
    void otherKnightsYouControlGetPlusOnePowerWhenBelleAttacks() {
        addReadyCreature(player1, new BelleOfTheBrawl());
        Permanent otherKnight = addReadyCreature(player1, new KnightOfTheKeep());
        Permanent nonKnight = addReadyCreature(player1, new GrizzlyBears());
        Permanent opposingKnight = addReadyCreature(player2, new KnightOfTheKeep());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(otherKnight.getEffectivePower()).isEqualTo(4);
        assertThat(otherKnight.getEffectiveToughness()).isEqualTo(2);
        assertThat(nonKnight.getEffectivePower()).isEqualTo(2);
        assertThat(opposingKnight.getEffectivePower()).isEqualTo(3);
    }

    @Test
    void BelleDoesNotBoostItself() {
        Permanent belle = addReadyCreature(player1, new BelleOfTheBrawl());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(belle.getEffectivePower()).isEqualTo(3);
        assertThat(belle.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        addReadyCreature(player1, new BelleOfTheBrawl());
        Permanent otherKnight = addReadyCreature(player1, new KnightOfTheKeep());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(otherKnight.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(otherKnight.getEffectivePower()).isEqualTo(3);
        assertThat(otherKnight.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
