package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NamelessOne.class, ElvishWarrior.class})
class NamelessOneTest extends BaseCardTest {

    @Test
    void powerAndToughnessEqualNumberOfWizardsOnAllBattlefields() {
        Permanent namelessOne = addCreatureReady(player1, new NamelessOne());

        assertThat(gqs.getEffectivePower(gd, namelessOne)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, namelessOne)).isEqualTo(1);

        addCreatureReady(player2, new NamelessOne());
        addCreatureReady(player1, new NamelessOne());

        assertThat(gqs.getEffectivePower(gd, namelessOne)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, namelessOne)).isEqualTo(3);
    }

    @Test
    void updatesWhenWizardsLeaveTheBattlefield() {
        Permanent namelessOne = addCreatureReady(player1, new NamelessOne());
        Permanent otherWizard = addCreatureReady(player2, new NamelessOne());

        assertThat(gqs.getEffectivePower(gd, namelessOne)).isEqualTo(2);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, otherWizard));

        assertThat(gqs.getEffectivePower(gd, namelessOne)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, namelessOne)).isEqualTo(1);
    }

    @Test
    void ignoresNonWizardsOnAllBattlefields() {
        Permanent namelessOne = addCreatureReady(player1, new NamelessOne());
        addCreatureReady(player2, new ElvishWarrior());
        addCreatureReady(player1, new ElvishWarrior());

        assertThat(gqs.getEffectivePower(gd, namelessOne)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, namelessOne)).isEqualTo(1);
    }

    @Test
    void canBeCastFaceDownAndTurnFaceUpForItsMorphCost() {
        harness.setHand(player1, List.of(new NamelessOne()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent namelessOne = findPermanent(player1, "Nameless One");
        assertThat(namelessOne.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, namelessOne)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, namelessOne)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(namelessOne));

        assertThat(namelessOne.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, namelessOne)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, namelessOne)).isEqualTo(1);
    }
}
