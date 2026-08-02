package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KurkeshOnakkeAncientTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {R} copies an artifact's activated ability — target takes damage twice")
    void payingCopiesArtifactAbility() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new KurkeshOnakkeAncient());
        Permanent rod = addReadyPermanent(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.RED, 4);

        int rodIndex = harness.getGameData().playerBattlefields.get(player1.getId()).indexOf(rod);
        harness.activateAbility(player1, rodIndex, null, player2.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);   // pay {R}, copy created
        harness.handleMayAbilityChosen(player1, false);  // keep the copy's original target

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining to pay leaves the ability uncopied")
    void decliningDoesNotCopy() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new KurkeshOnakkeAncient());
        Permanent rod = addReadyPermanent(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.RED, 4);

        int rodIndex = harness.getGameData().playerBattlefields.get(player1.getId()).indexOf(rod);
        harness.activateAbility(player1, rodIndex, null, player2.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The copy may be given a new target")
    void copyMayChooseNewTarget() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new KurkeshOnakkeAncient());
        Permanent rod = addReadyPermanent(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addToBattlefield(player2, new LlanowarElves());

        int rodIndex = harness.getGameData().playerBattlefields.get(player1.getId()).indexOf(rod);
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        harness.activateAbility(player1, rodIndex, null, player2.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, elvesId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A nonartifact source's ability does not trigger Kurkesh")
    void nonArtifactAbilityDoesNotTrigger() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new KurkeshOnakkeAncient());
        Permanent pyro = addReadyPermanent(player1, new ProdigalPyromancer());
        harness.addMana(player1, ManaColor.RED, 4);

        int pyroIndex = harness.getGameData().playerBattlefields.get(player1.getId()).indexOf(pyro);
        harness.activateAbility(player1, pyroIndex, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.pendingMayAbilities).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A mana ability does not trigger Kurkesh")
    void manaAbilityDoesNotTrigger() {
        harness.addToBattlefield(player1, new KurkeshOnakkeAncient());
        Permanent elves = addReadyPermanent(player1, new LlanowarElves());

        int elvesIndex = harness.getGameData().playerBattlefields.get(player1.getId()).indexOf(elves);
        harness.tapPermanent(player1, elvesIndex);

        GameData gd = harness.getGameData();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private Permanent addReadyPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
