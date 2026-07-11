package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RingsOfBrighthearthTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} copies the activated ability — target takes damage twice")
    void payingCopiesAbility() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RingsOfBrighthearth());
        addReadyPyromancer(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        int pyroIndex = harness.getGameData().playerBattlefields.get(player1.getId()).size() - 1;
        harness.activateAbility(player1, pyroIndex, null, player2.getId());

        // Rings' trigger resolves first, offering "pay {2} to copy".
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);   // pay {2}, copy created
        harness.handleMayAbilityChosen(player1, false);  // keep the copy's original target

        // Resolve the copy, then the original ability.
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining to pay leaves the ability uncopied — target takes damage once")
    void decliningDoesNotCopy() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RingsOfBrighthearth());
        addReadyPyromancer(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        int pyroIndex = harness.getGameData().playerBattlefields.get(player1.getId()).size() - 1;
        harness.activateAbility(player1, pyroIndex, null, player2.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);  // decline to pay

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The copy may be given a new target")
    void copyMayChooseNewTarget() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RingsOfBrighthearth());
        addReadyPyromancer(player1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addToBattlefield(player2, new LlanowarElves());

        int pyroIndex = harness.getGameData().playerBattlefields.get(player1.getId()).size() - 1;
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        harness.activateAbility(player1, pyroIndex, null, player2.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);   // pay {2}
        harness.handleMayAbilityChosen(player1, true);   // choose new targets for the copy
        harness.handlePermanentChosen(player1, elvesId); // copy now hits the Elves

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        // Copy killed the 1/1; the original ability still hit player2 for 1.
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Mana abilities do not trigger Rings of Brighthearth")
    void manaAbilityDoesNotTrigger() {
        harness.addToBattlefield(player1, new RingsOfBrighthearth());
        Permanent elves = addReadyElves(player1);

        int elvesIndex = harness.getGameData().playerBattlefields.get(player1.getId()).indexOf(elves);
        harness.tapPermanent(player1, elvesIndex);

        GameData gd = harness.getGameData();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private Permanent addReadyPyromancer(Player player) {
        Permanent perm = new Permanent(new ProdigalPyromancer());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyElves(Player player) {
        Permanent perm = new Permanent(new LlanowarElves());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
