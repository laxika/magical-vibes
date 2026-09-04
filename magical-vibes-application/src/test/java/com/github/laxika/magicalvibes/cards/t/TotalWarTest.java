package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.g.GlacialWall;
import com.github.laxika.magicalvibes.cards.m.Melee;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TotalWar.class, BalduvianBears.class})
class TotalWarTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the attacking player's untapped non-attacking creatures")
    void destroysStayHomeCreatures() {
        harness.addToBattlefield(player1, new TotalWar());
        Permanent attacker = addCreatureReady(player2, new BalduvianBears());
        Permanent stayedHome = addCreatureReady(player2, new BalduvianBears());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(attacker.getId())
                .doesNotContain(stayedHome.getId());
    }

    @Test
    @DisplayName("Does not trigger when the attacking player declares no attackers")
    void doesNotTriggerForEmptyAttack() {
        harness.addToBattlefield(player1, new TotalWar());
        Permanent stayedHome = addCreatureReady(player2, new BalduvianBears());

        declareAttackers(player2, List.of());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(stayedHome.getId());
    }

    @Test
    @CardUsed(Melee.class)
    @DisplayName("Spares a creature that attacked before being removed from combat")
    void sparesCreatureThatWasRemovedFromCombat() {
        harness.addToBattlefield(player1, new TotalWar());
        addCreatureReady(player1, new BalduvianBears());
        Permanent attacker = addCreatureReady(player2, new BalduvianBears());

        declareAttackers(player2, List.of(0));
        harness.setHand(player2, List.of(new Melee()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.declareBlockers(gd, player2, List.of());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(attacker.getId());
    }

    @Test
    @CardUsed(GlacialWall.class)
    @DisplayName("Spares Walls, tapped creatures and creatures that arrived this turn")
    void sparesExemptCreatures() {
        harness.addToBattlefield(player1, new TotalWar());
        addCreatureReady(player2, new BalduvianBears());
        Permanent wall = addCreatureReady(player2, new GlacialWall());
        Permanent tapped = addCreatureReady(player2, new BalduvianBears());
        tapped.tap();
        Permanent freshlyArrived = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(wall.getId(), tapped.getId(), freshlyArrived.getId());
    }

    @Test
    @DisplayName("Leaves the non-attacking player's creatures alone")
    void ignoresNonAttackingPlayer() {
        harness.addToBattlefield(player1, new TotalWar());
        Permanent defenderCreature = addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player2, new BalduvianBears());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .contains(defenderCreature.getId());
    }
}
