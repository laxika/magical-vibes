package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IllusionistsBracersTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature's non-mana ability is copied for free — target takes damage twice")
    void copiesEquippedCreatureAbility() {
        harness.setLife(player2, 20);
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());
        Permanent bracers = addReady(player1, new IllusionistsBracers());
        bracers.setAttachedTo(pyromancer.getId());

        activate(player1, pyromancer, player2.getId());

        // The Bracers trigger resolves first and copies the ability with no cost.
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false); // keep the copy's original target

        harness.passBothPriorities(); // copy
        harness.passBothPriorities(); // original

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The copy may be given a new target")
    void copyMayChooseNewTarget() {
        harness.setLife(player2, 20);
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());
        Permanent bracers = addReady(player1, new IllusionistsBracers());
        bracers.setAttachedTo(pyromancer.getId());
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        activate(player1, pyromancer, player2.getId());

        harness.passBothPriorities();
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
    @DisplayName("An unattached Bracers copies nothing")
    void unattachedBracersDoesNotTrigger() {
        harness.setLife(player2, 20);
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());
        addReady(player1, new IllusionistsBracers());

        activate(player1, pyromancer, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("An ability of a creature the Bracers is not attached to isn't copied")
    void otherCreaturesAbilityNotCopied() {
        harness.setLife(player2, 20);
        Permanent equipped = addReady(player1, new ProdigalPyromancer());
        Permanent other = addReady(player1, new ProdigalPyromancer());
        Permanent bracers = addReady(player1, new IllusionistsBracers());
        bracers.setAttachedTo(equipped.getId());

        activate(player1, other, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Mana abilities of the equipped creature are not copied")
    void manaAbilityNotCopied() {
        Permanent elves = addReady(player1, new LlanowarElves());
        Permanent bracers = addReady(player1, new IllusionistsBracers());
        bracers.setAttachedTo(elves.getId());

        int elvesIndex = harness.getGameData().playerBattlefields.get(player1.getId()).indexOf(elves);
        harness.tapPermanent(player1, elvesIndex);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip {3} attaches the Bracers to a creature you control")
    void equipAttachesToCreature() {
        Permanent creature = addReady(player1, new LlanowarElves());
        Permanent bracers = addReady(player1, new IllusionistsBracers());
        harness.addMana(player1, ManaColor.WHITE, 3);

        int bracersIndex = harness.getGameData().playerBattlefields.get(player1.getId()).indexOf(bracers);
        harness.activateAbility(player1, bracersIndex, null, creature.getId());
        harness.passBothPriorities();

        assertThat(bracers.getAttachedTo()).isEqualTo(creature.getId());
    }

    private void activate(Player player, Permanent permanent, UUID targetId) {
        int index = harness.getGameData().playerBattlefields.get(player.getId()).indexOf(permanent);
        harness.activateAbility(player, index, null, targetId);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
