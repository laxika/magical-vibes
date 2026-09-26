package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KayaGhostAssassin.class, GrizzlyBears.class, Shock.class})
class KayaGhostAssassinTest extends BaseCardTest {

    @Test
    void zeroExilesKayaAndReturnsHerAtControllersNextUpkeep() {
        Permanent kaya = addReadyKaya(3);

        harness.activateAbility(player1, battlefieldIndex(kaya), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kaya);
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getName)
                .contains("Kaya, Ghost Assassin");
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);

        advanceToUpkeep(player2);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getName().equals("Kaya, Ghost Assassin"));

        advanceToUpkeep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard().getName().equals("Kaya, Ghost Assassin"));
    }

    @Test
    void zeroCanExileTargetCreatureAndReturnItAtControllersNextUpkeep() {
        addReadyKaya(3);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature.getCard());
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);

        advanceToUpkeep(player2);
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(
                permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        advanceToUpkeep(player1);
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(
                permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    void minusOneDrainsEachOpponent() {
        addReadyKaya(3);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void minusTwoMakesEachOpponentDiscardAndDraws() {
        addReadyKaya(3);
        Card opponentCard = new Shock();
        harness.setHand(player2, List.of(opponentCard));
        harness.setLibrary(player1, List.of(new Shock()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).contains("Shock");
    }

    private Permanent addReadyKaya(int loyalty) {
        Permanent kaya = new Permanent(new KayaGhostAssassin());
        kaya.setCounterCount(CounterType.LOYALTY, loyalty);
        kaya.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(kaya);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return kaya;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
