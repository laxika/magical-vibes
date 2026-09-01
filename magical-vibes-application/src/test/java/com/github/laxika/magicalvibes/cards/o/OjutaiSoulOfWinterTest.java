package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OjutaiSoulOfWinter.class, Forest.class, GrizzlyBears.class})
class OjutaiSoulOfWinterTest extends BaseCardTest {

    @Test
    @DisplayName("An attacking Dragon taps and locks an opponent's nonland permanent")
    void attackingDragonTapsAndLocksTarget() {
        addReadyOjutai();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Dragon attacker does not trigger Ojutai")
    void nonDragonDoesNotTrigger() {
        addReadyOjutai();
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(target.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("An opponent's land cannot be chosen as the target")
    void opponentLandIsNotALegalTarget() {
        addReadyOjutai();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(land.isTapped()).isFalse();
        assertThat(land.getSkipUntapCount()).isZero();
    }

    private Permanent addReadyOjutai() {
        Permanent permanent = new Permanent(new OjutaiSoulOfWinter());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
