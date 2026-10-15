package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JungleWeaver;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhereAncientsTread.class, CrawWurm.class, HillGiant.class, SerraAngel.class,
        JungleWeaver.class, Unsummon.class})
class WhereAncientsTreadTest extends BaseCardTest {

    @Test
    @DisplayName("Big creature entering lets controller deal 5 damage to a player")
    void bigCreatureDamagesPlayer() {
        harness.addToBattlefield(player1, new WhereAncientsTread());

        harness.castFromHand(player1, new CrawWurm(), "{4}{G}{G}");
        harness.passBothPriorities(); // resolve Craw Wurm (6/4)

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passBothPriorities(); // choose the triggered ability's target
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve the ability and open the may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);
    }

    @Test
    @DisplayName("Big creature entering lets controller deal 5 damage to a creature")
    void bigCreatureDamagesCreature() {
        harness.addToBattlefield(player1, new WhereAncientsTread());
        harness.addToBattlefield(player2, new SerraAngel());
        UUID angelId = harness.getPermanentId(player2, "Serra Angel");

        harness.castFromHand(player1, new CrawWurm(), "{4}{G}{G}");
        harness.passBothPriorities(); // resolve Craw Wurm

        harness.passBothPriorities(); // choose the triggered ability's target
        harness.handlePermanentChosen(player1, angelId);
        harness.passBothPriorities(); // resolve the ability and open the may prompt
        harness.handleMayAbilityChosen(player1, true);

        // Serra Angel (4/4) dies to 5 damage
        harness.assertNotOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Declining deals no damage")
    void decliningDealsNoDamage() {
        harness.addToBattlefield(player1, new WhereAncientsTread());

        harness.castFromHand(player1, new CrawWurm(), "{4}{G}{G}");
        harness.passBothPriorities(); // resolve Craw Wurm

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passBothPriorities(); // choose the triggered ability's target
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve the ability and open the may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creature with power less than 5 does not trigger")
    void lowPowerDoesNotTrigger() {
        harness.addToBattlefield(player1, new WhereAncientsTread());

        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        harness.passBothPriorities(); // resolve Hill Giant (3/3)

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("A creature with exactly five power triggers")
    void exactlyFivePowerTriggers() {
        harness.addToBattlefield(player1, new WhereAncientsTread());
        harness.castFromHand(player1, new JungleWeaver(), "{5}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("An opponent's large creature does not trigger")
    void opponentsCreatureDoesNotTrigger() {
        harness.addToBattlefield(player2, new WhereAncientsTread());
        harness.castFromHand(player1, new JungleWeaver(), "{5}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Damage still resolves after the entering creature leaves")
    void enteringCreatureLeavingDoesNotStopDamage() {
        harness.addToBattlefield(player1, new WhereAncientsTread());
        harness.castFromHand(player1, new JungleWeaver(), "{5}{G}{G}");
        harness.passBothPriorities();
        UUID weaverId = harness.getPermanentId(player1, "Jungle Weaver");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, weaverId);
        harness.assertNotOnBattlefield(player1, "Jungle Weaver");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player2, 15);
    }
}
