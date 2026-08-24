package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RankleAndTorbran.class, GrizzlyBears.class, InvasionOfInnistrad.class,
        SerraAngel.class, Shock.class})
class RankleAndTorbranTest extends BaseCardTest {

    private static final String TREASURE = "Each player creates a Treasure token.";
    private static final String SACRIFICE = "Each player sacrifices a creature of their choice.";
    private static final String DAMAGE =
            "If a source would deal damage to a player or battle this turn, it deals that much damage plus 2 instead.";

    @Test
    @DisplayName("Combat damage trigger can make each player create a Treasure")
    void createsTreasureForEachPlayer() {
        addAttackingRankle();
        resolveCombatAndTrigger();

        harness.handleMayAbilityChosen(player1, true);
        chooseMode(TREASURE);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player2, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage trigger gathers each player's creature choice before sacrificing")
    void sacrificesOneCreatureFromEachPlayer() {
        addAttackingRankle();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        resolveCombatAndTrigger();

        harness.handleMayAbilityChosen(player1, true);
        chooseMode(SACRIFICE);
        harness.handleMultiplePermanentsChosen(player1, List.of(ownCreature.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Rankle and Torbran"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentCreature.getId()));
    }

    @Test
    @DisplayName("Damage mode adds two to player and battle damage but not creature damage")
    void addsDamageOnlyToPlayersAndBattles() {
        addAttackingRankle();
        resolveCombatAndTrigger();

        harness.handleMayAbilityChosen(player1, true);
        chooseMode(DAMAGE);

        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfInnistrad());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, battle.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    private void addAttackingRankle() {
        Permanent rankle = addCreatureReady(player1, new RankleAndTorbran());
        rankle.setAttacking(true);
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }

    private void chooseMode(String mode) {
        harness.handleListChoice(player1, mode);
        harness.handleListChoice(player1, "Done");
    }
}
