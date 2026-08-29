package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArchangelElspeth.class, GrizzlyBears.class, HillGiant.class, Plains.class, Shock.class})
class ArchangelElspethTest extends BaseCardTest {

    @Test
    @DisplayName("+1 creates a lifelinking white Soldier token")
    void plusOneCreatesLifelinkingSoldier() {
        Permanent elspeth = addReadyElspeth(4);

        harness.activateAbility(player1, indexOf(elspeth), 0, null, null);
        harness.passBothPriorities();

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getSubtypes().contains(CardSubtype.SOLDIER)
                        && permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1
                        && gqs.hasKeyword(gd, permanent, Keyword.LIFELINK));
    }

    @Test
    @DisplayName("-2 adds counters, Angel, and indefinite flying to a target creature")
    void minusTwoEnhancesTargetCreature() {
        Permanent elspeth = addReadyElspeth(4);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(elspeth), 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).contains(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).contains(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("-2 cannot target a noncreature permanent")
    void minusTwoRejectsNoncreaturePermanent() {
        Permanent elspeth = addReadyElspeth(4);
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(elspeth), 1, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-6 returns qualifying nonland permanents and leaves other cards in the graveyard")
    void minusSixReturnsQualifyingPermanents() {
        Permanent elspeth = addReadyElspeth(6);
        Card bears = new GrizzlyBears();
        Card giant = new HillGiant();
        Card plains = new Plains();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(bears, giant, plains, shock));

        harness.activateAbility(player1, indexOf(elspeth), 2, null, null);
        harness.passBothPriorities();

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(giant, plains, shock)
                .doesNotContain(bears);
    }

    private Permanent addReadyElspeth(int loyalty) {
        Permanent elspeth = new Permanent(new ArchangelElspeth());
        elspeth.setCounterCount(CounterType.LOYALTY, loyalty);
        elspeth.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(elspeth);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return elspeth;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
