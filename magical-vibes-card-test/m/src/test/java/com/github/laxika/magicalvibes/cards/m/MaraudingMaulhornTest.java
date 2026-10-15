package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AdvocateOfTheBeast;
import com.github.laxika.magicalvibes.cards.r.RumblingBaloth;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaraudingMaulhorn.class, AdvocateOfTheBeast.class, RumblingBaloth.class})
class MaraudingMaulhornTest extends BaseCardTest {

    private Permanent addReady(com.github.laxika.magicalvibes.model.Card card,
                               com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    @DisplayName("Marauding Maulhorn must attack when its controller has no Advocate of the Beast")
    void mustAttackWithoutAdvocate() {
        addReady(new MaraudingMaulhorn(), player1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Marauding Maulhorn may stay home while its controller controls Advocate of the Beast")
    void notForcedWithAdvocate() {
        addReady(new MaraudingMaulhorn(), player1);
        addReady(new AdvocateOfTheBeast(), player1);

        assertThatCode(() -> declareAttackers(player1, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("An opponent's Advocate of the Beast does not free Marauding Maulhorn")
    void opponentAdvocateDoesNotHelp() {
        addReady(new MaraudingMaulhorn(), player1);
        addReady(new AdvocateOfTheBeast(), player2);

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("A differently named creature does not free Marauding Maulhorn")
    void otherCreatureDoesNotHelp() {
        addReady(new MaraudingMaulhorn(), player1);
        addReady(new RumblingBaloth(), player1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Marauding Maulhorn attacking satisfies the requirement")
    void attackingIsLegal() {
        harness.setLife(player2, 20);
        addReady(new MaraudingMaulhorn(), player1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("A tapped Maulhorn is not required to attack")
    void tappedMaulhornMayStayHome() {
        addReady(new MaraudingMaulhorn(), player1).tap();

        assertThatCode(() -> declareAttackers(player1, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A summoning-sick Maulhorn is not required to attack")
    void summoningSickMaulhornMayStayHome() {
        Permanent maulhorn = addReady(new MaraudingMaulhorn(), player1);
        maulhorn.setSummoningSick(true);

        assertThatCode(() -> declareAttackers(player1, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A tapped Advocate still exempts Maulhorn from attacking")
    void tappedAdvocateStillHelps() {
        addReady(new MaraudingMaulhorn(), player1);
        addReady(new AdvocateOfTheBeast(), player1).tap();

        assertThatCode(() -> declareAttackers(player1, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Maulhorn must attack once its controller's Advocate leaves the battlefield")
    void advocateLeavingRestoresRequirement() {
        addReady(new MaraudingMaulhorn(), player1);
        Permanent advocate = addReady(new AdvocateOfTheBeast(), player1);
        gd.playerBattlefields.get(player1.getId()).remove(advocate);
        gd.playerGraveyards.get(player1.getId()).add(advocate.getCard());

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }
}
