package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.d.DeathriteShaman;
import com.github.laxika.magicalvibes.cards.z.ZameckGuildmage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IllusionistsBracers.class, LlanowarElves.class, ProdigalPyromancer.class,
        ZameckGuildmage.class, DeathriteShaman.class})
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

    @Test
    void copiesUntargetedAbilityWithoutPayingItsManaCostAgain() {
        Permanent guildmage = addReady(player1, new ZameckGuildmage());
        Permanent bracers = addReady(player1, new IllusionistsBracers());
        bracers.setAttachedTo(guildmage.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        activate(player1, guildmage, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new LlanowarElves());
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bracersControllerControlsCopyOfOpponentsCreatureAbility() {
        Permanent guildmage = addReady(player2, new ZameckGuildmage());
        Permanent bracers = addReady(player1, new IllusionistsBracers());
        bracers.setAttachedTo(guildmage.getId());
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.ensurePriority(player2);

        activate(player2, guildmage, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent ownCreature = harness.enterBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opposingCreature = harness.enterBattlefieldAndReturn(player2, new LlanowarElves());
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyCanChooseAnotherCreatureCardInGraveyard() {
        Permanent shaman = addReady(player1, new DeathriteShaman());
        Permanent bracers = addReady(player1, new IllusionistsBracers());
        bracers.setAttachedTo(shaman.getId());
        Card originalTarget = new LlanowarElves();
        Card newTarget = new LlanowarElves();
        harness.setGraveyard(player2, List.of(originalTarget, newTarget));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shaman),
                2, null, originalTarget.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, newTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(originalTarget, newTarget);
        harness.assertLife(player1, 14);
        assertThat(gd.stack).isEmpty();
    }

    private void activate(Player player, Permanent permanent, UUID targetId) {
        int index = harness.getGameData().playerBattlefields.get(player.getId()).indexOf(permanent);
        harness.activateAbility(player, index, null, targetId);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }
}
