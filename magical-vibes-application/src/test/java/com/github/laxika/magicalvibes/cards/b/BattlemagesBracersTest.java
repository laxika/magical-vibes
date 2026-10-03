package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LivingInferno;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattlemagesBracers.class, LlanowarElves.class, ProdigalPyromancer.class, LivingInferno.class})
class BattlemagesBracersTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has haste")
    void equippedCreatureHasHaste() {
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        Permanent bracers = addCreatureReady(player1, new BattlemagesBracers());
        bracers.setAttachedTo(pyromancer.getId());

        assertThat(gqs.hasKeyword(gd, pyromancer, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature's non-mana ability may be copied for {1}")
    void mayPayToCopyEquippedCreatureAbility() {
        harness.setLife(player2, 20);
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        Permanent bracers = addCreatureReady(player1, new BattlemagesBracers());
        bracers.setAttachedTo(pyromancer.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate(player1, pyromancer, player2.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Mana abilities of the equipped creature are not copied")
    void manaAbilityIsNotCopied() {
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        Permanent bracers = addCreatureReady(player1, new BattlemagesBracers());
        bracers.setAttachedTo(elves.getId());

        int elvesIndex = harness.getGameData().playerBattlefields.get(player1.getId()).indexOf(elves);
        harness.tapPermanent(player1, elvesIndex);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.pendingMayAbilities).isEmpty();
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip {2} attaches the Bracers to a creature you control")
    void equipAttachesToCreature() {
        Permanent creature = addCreatureReady(player1, new LlanowarElves());
        Permanent bracers = addCreatureReady(player1, new BattlemagesBracers());
        harness.addMana(player1, ManaColor.WHITE, 2);

        int bracersIndex = harness.getGameData().playerBattlefields.get(player1.getId()).indexOf(bracers);
        harness.activateAbility(player1, bracersIndex, null, creature.getId());
        harness.passBothPriorities();

        assertThat(bracers.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void mayDeclineCopyWithoutSpendingMana() {
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        Permanent bracers = harness.addToBattlefieldAndReturn(player1, new BattlemagesBracers());
        bracers.setAttachedTo(pyromancer.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate(player1, pyromancer, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void copyMayChooseNewTarget() {
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        Permanent bracers = harness.addToBattlefieldAndReturn(player1, new BattlemagesBracers());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        bracers.setAttachedTo(pyromancer.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate(player1, pyromancer, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, elves.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unequippedCreaturesAbilityDoesNotTrigger() {
        Permanent equipped = addCreatureReady(player1, new ProdigalPyromancer());
        Permanent other = addCreatureReady(player1, new ProdigalPyromancer());
        Permanent bracers = harness.addToBattlefieldAndReturn(player1, new BattlemagesBracers());
        bracers.setAttachedTo(equipped.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate(player1, other, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void multiTargetCopyOffersNewTargets() {
        Permanent inferno = addCreatureReady(player1, new LivingInferno());
        Permanent bracers = harness.addToBattlefieldAndReturn(player1, new BattlemagesBracers());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        bracers.setAttachedTo(inferno.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(first.getId(), 4, second.getId(), 4));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput())
                .as("The controller must be offered a choice of new targets for the copy")
                .isTrue();
    }

    @Test
    void newlyEnteredEquippedCreatureCanActivateTapAbility() {
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(true);
        Permanent bracers = harness.addToBattlefieldAndReturn(player1, new BattlemagesBracers());
        bracers.setAttachedTo(pyromancer.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate(player1, pyromancer, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(pyromancer.isTapped()).isTrue();
        harness.assertLife(player2, 19);
    }

    @Test
    void equipmentControllerPaysForCopyOfOpponentsAbility() {
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        Permanent bracers = harness.addToBattlefieldAndReturn(player1, new BattlemagesBracers());
        bracers.setAttachedTo(pyromancer.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate(player2, pyromancer, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void activate(Player player, Permanent permanent, UUID targetId) {
        int index = harness.getGameData().playerBattlefields.get(player.getId()).indexOf(permanent);
        harness.activateAbility(player, index, null, targetId);
    }

}
