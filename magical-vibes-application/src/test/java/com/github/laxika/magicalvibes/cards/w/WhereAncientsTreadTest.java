package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WhereAncientsTreadTest extends BaseCardTest {

    // ===== Power 5+ creature entering triggers the may-damage =====

    @Test
    @DisplayName("Big creature entering lets controller deal 5 damage to a player")
    void bigCreatureDamagesPlayer() {
        harness.addToBattlefield(player1, new WhereAncientsTread());

        harness.setHand(player1, List.of(new CrawWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Craw Wurm (6/4)

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passBothPriorities(); // resolve the MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);
    }

    @Test
    @DisplayName("Big creature entering lets controller deal 5 damage to a creature")
    void bigCreatureDamagesCreature() {
        harness.addToBattlefield(player1, new WhereAncientsTread());
        harness.addToBattlefield(player2, new SerraAngel());
        UUID angelId = harness.getPermanentId(player2, "Serra Angel");

        harness.setHand(player1, List.of(new CrawWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Craw Wurm

        harness.passBothPriorities(); // resolve the MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, angelId);

        // Serra Angel (4/4) dies to 5 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Serra Angel"));
    }

    @Test
    @DisplayName("Declining deals no damage")
    void decliningDealsNoDamage() {
        harness.addToBattlefield(player1, new WhereAncientsTread());

        harness.setHand(player1, List.of(new CrawWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Craw Wurm

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passBothPriorities(); // resolve the MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    // ===== Power < 5 does not trigger =====

    @Test
    @DisplayName("Creature with power less than 5 does not trigger")
    void lowPowerDoesNotTrigger() {
        harness.addToBattlefield(player1, new WhereAncientsTread());

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Hill Giant (3/3)

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
