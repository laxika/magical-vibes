package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.s.SummitProwler;
import com.github.laxika.magicalvibes.cards.t.ThassaGodOfTheSea;
import com.github.laxika.magicalvibes.cards.w.WetlandSambar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarrageOfBoulders.class, AlpineGrizzly.class, SummitProwler.class, WetlandSambar.class,
        ThassaGodOfTheSea.class})
class BarrageOfBouldersTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each creature the caster does not control")
    void damagesOnlyOpponentsCreatures() {
        Permanent own = addCreatureReady(player1, makeCreature("Own Creature", 2, 2));
        Permanent opponent = addCreatureReady(player2, makeCreature("Opponent Creature", 2, 2));

        castBarrage();

        assertThat(own.getMarkedDamage()).isZero();
        assertThat(opponent.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not stop creatures from blocking without ferocious")
    void doesNotPreventBlockingWithoutFerocious() {
        Permanent attacker = addCreatureReady(player1, makeCreature("Attacker", 3, 3));
        Permanent blocker = addCreatureReady(player2, makeCreature("Blocker", 2, 2));

        castBarrage();
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Stops all creatures from blocking when ferocious is active")
    void preventsBlockingWithFerocious() {
        Permanent attacker = addCreatureReady(player1, makeCreature("Attacker", 4, 4));
        addCreatureReady(player2, makeCreature("Blocker", 2, 2));

        castBarrage();
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castBarrage() {
        harness.setHand(player1, List.of(new BarrageOfBoulders()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, (UUID) null);
    }

    @Test
    @DisplayName("Kills one-toughness opposing creatures without damaging either player")
    void lethalDamageDoesNotDamagePlayers() {
        addCreatureReady(player1, new WetlandSambar());
        addCreatureReady(player2, new WetlandSambar());
        int ownLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        castBarrage();

        harness.assertOnBattlefield(player1, "Wetland Sambar");
        harness.assertNotOnBattlefield(player2, "Wetland Sambar");
        harness.assertInGraveyard(player2, "Wetland Sambar");
        harness.assertLife(player1, ownLife);
        harness.assertLife(player2, opponentLife);
    }

    @Test
    @DisplayName("An opponent's four-power creature does not enable ferocious")
    void opponentsPowerDoesNotEnableFerocious() {
        Permanent attacker = addCreatureReady(player1, new WetlandSambar());
        Permanent blocker = addCreatureReady(player2, new SummitProwler());

        castBarrage();
        attacker.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A God that is not a creature does not enable ferocious")
    void noncreatureGodDoesNotEnableFerocious() {
        addCreatureReady(player1, new ThassaGodOfTheSea());
        Permanent attacker = addCreatureReady(player1, new WetlandSambar());
        Permanent blocker = addCreatureReady(player2, new SummitProwler());

        castBarrage();
        attacker.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Ferocious is checked at resolution rather than when the spell is cast")
    void losingFerociousBeforeResolutionAllowsBlocking() {
        Permanent ferocious = addCreatureReady(player1, new AlpineGrizzly());
        Permanent attacker = addCreatureReady(player1, new WetlandSambar());
        Permanent blocker = addCreatureReady(player2, new SummitProwler());
        harness.setHand(player1, List.of(new BarrageOfBoulders()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(ferocious);
        gd.playerGraveyards.get(player1.getId()).add(ferocious.getCard());

        harness.passBothPriorities();
        attacker.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The restriction persists after losing ferocious and affects later creatures")
    void laterCreaturesCannotBlockAfterFerociousCreatureLeaves() {
        Permanent ferocious = addCreatureReady(player1, new AlpineGrizzly());
        Permanent attacker = addCreatureReady(player1, new WetlandSambar());

        castBarrage();
        gd.playerBattlefields.get(player1.getId()).remove(ferocious);
        gd.playerGraveyards.get(player1.getId()).add(ferocious.getCard());
        addCreatureReady(player2, new SummitProwler());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private Card makeCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
