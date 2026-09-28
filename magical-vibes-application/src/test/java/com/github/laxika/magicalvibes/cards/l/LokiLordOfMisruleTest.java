package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.ThrunTheLastTroll;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LokiLordOfMisrule.class, GrizzlyBears.class, HillGiant.class, ThrunTheLastTroll.class})
class LokiLordOfMisruleTest extends BaseCardTest {

    @Test
    @DisplayName("Copies each other creature you control and removes legendary")
    void copiesOtherControlledCreaturesWithoutLegendary() {
        Permanent loki = addReady(player1, new LokiLordOfMisrule());
        Permanent target = addReady(player1, new ThrunTheLastTroll());
        Permanent other = addReady(player1, new GrizzlyBears());
        Permanent opponent = addReady(player2, new GrizzlyBears());

        activate(loki, target);

        assertThat(target.getCard().getName()).isEqualTo("Thrun, the Last Troll");
        assertThat(loki.getCard().getName()).isEqualTo("Thrun, the Last Troll");
        assertThat(other.getCard().getName()).isEqualTo("Thrun, the Last Troll");
        assertThat(loki.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(other.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(opponent.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Temporary copies revert at cleanup")
    void copiesRevertAtEndOfTurn() {
        Permanent loki = addReady(player1, new LokiLordOfMisrule());
        Permanent target = addReady(player1, new HillGiant());
        Permanent other = addReady(player1, new GrizzlyBears());

        activate(loki, target);
        assertThat(loki.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(other.getCard().getName()).isEqualTo("Hill Giant");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(loki.getCard().getName()).isEqualTo("Loki, Lord of Misrule");
        assertThat(other.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    private void activate(Permanent loki, Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, battlefieldIndex(player1, loki), null, target.getId());
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
