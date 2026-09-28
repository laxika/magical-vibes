package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WyllPactBoundDuelist.class, Forest.class, GrizzlyBears.class, Island.class,
        Mountain.class, Plains.class, Swamp.class})
class WyllPactBoundDuelistTest extends BaseCardTest {

    @Test
    void hasFiveSpecializeAbilities() {
        Permanent wyll = addCreatureReady(player1, new WyllPactBoundDuelist());

        assertThat(wyll.getCard().getActivatedAbilities()).hasSize(5);
    }

    @Test
    void specializesIntoCelestialPact() {
        assertThat(specialize(0, new Plains()).getCard().getName())
                .isEqualTo("Wyll of the Celestial Pact");
    }

    @Test
    void specializesIntoElderPact() {
        assertThat(specialize(1, new Island()).getCard().getName())
                .isEqualTo("Wyll of the Elder Pact");
    }

    @Test
    void specializesIntoFiendPact() {
        assertThat(specialize(2, new Swamp()).getCard().getName())
                .isEqualTo("Wyll of the Fiend Pact");
    }

    @Test
    void specializesIntoBladePact() {
        assertThat(specialize(3, new Mountain()).getCard().getName())
                .isEqualTo("Wyll of the Blade Pact");
    }

    @Test
    void specializesIntoFeyPact() {
        assertThat(specialize(4, new Forest()).getCard().getName())
                .isEqualTo("Wyll of the Fey Pact");
    }

    private Permanent specialize(int abilityIndex, Card discard) {
        Permanent wyll = addCreatureReady(player1, new WyllPactBoundDuelist());
        harness.setHand(player1, List.of(discard));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        int wyllIndex = gd.playerBattlefields.get(player1.getId()).size() - 1;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, wyllIndex, abilityIndex, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return wyll;
    }
}
