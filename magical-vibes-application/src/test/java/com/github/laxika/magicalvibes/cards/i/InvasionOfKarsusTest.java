package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RefractionElemental;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GarrukWildspeaker.class, GrizzlyBears.class, InvasionOfKarsus.class, RefractionElemental.class})
class InvasionOfKarsusTest extends BaseCardTest {

    @Test
    @DisplayName("The Siege deals 3 damage to each creature and planeswalker when it enters")
    void etbDealsDamageToCreaturesAndPlaneswalkers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        castInvasion();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Defeating the Siege exiles it and casts Refraction Elemental transformed")
    void defeatCastsBackFace() {
        castInvasion();

        Permanent battle = findPermanentByName(player1, "Invasion of Karsus");
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent backFace = findPermanentByName(player1, "Refraction Elemental");
        assertThat(backFace.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("The back face deals 2 damage to each opponent whenever its controller casts a spell")
    void backFaceDamagesEachOpponentOnSpellCast() {
        InvasionOfKarsus front = new InvasionOfKarsus();
        harness.addToBattlefield(player1, front.getBackFaceCard());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int opponentLife = gd.getLife(player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 2);
    }

    private void castInvasion() {
        Card invasion = new InvasionOfKarsus();
        harness.setHand(player1, List.of(invasion));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findPermanentByName(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }
}
