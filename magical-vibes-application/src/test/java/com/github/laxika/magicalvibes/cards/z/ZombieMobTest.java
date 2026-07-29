package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZombieMobTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each creature card in controller's graveyard")
    void entersWithCountersPerCreatureCard() {
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new Shock()); // not a creature card

        castMob();

        Permanent mob = findMob();
        assertThat(mob).isNotNull();
        assertThat(mob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exiles all creature cards from controller's graveyard, leaving non-creature cards")
    void exilesCreatureCardsFromGraveyard() {
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new Shock());

        castMob();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Grizzly Bears", "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not touch the opponent's graveyard")
    void leavesOpponentGraveyardAlone() {
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());

        castMob();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(findMob().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("With an empty graveyard it enters as a 2/0 and dies to state-based actions")
    void diesWithNoCreatureCardsInGraveyard() {
        castMob();

        harness.assertNotOnBattlefield(player1, "Zombie Mob");
    }

    private void castMob() {
        harness.setHand(player1, List.of(new ZombieMob()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 2); // 2 generic

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // resolve the creature spell
        harness.passBothPriorities(); // resolve the enters-the-battlefield exile trigger
    }

    private Permanent findMob() {
        return findMob(player1);
    }

    private Permanent findMob(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie Mob"))
                .findFirst().orElse(null);
    }
}
