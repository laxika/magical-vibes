package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BroodKeeper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpiritLink;
import com.github.laxika.magicalvibes.cards.s.SwordOfTheRealms;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HalvarGodOfBattle.class, GrizzlyBears.class, SpiritLink.class, SwordOfTheRealms.class,
        BroodKeeper.class})
class HalvarGodOfBattleTest extends BaseCardTest {

    @Test
    void enchantedCreaturesHaveDoubleStrike() {
        harness.addToBattlefield(player1, new HalvarGodOfBattle());
        Permanent creature = addCreatureReady(player1);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SpiritLink());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void beginningOfCombatMayMoveAttachedEquipment() {
        harness.addToBattlefield(player1, new HalvarGodOfBattle());
        Permanent firstCreature = addCreatureReady(player1);
        Permanent secondCreature = addCreatureReady(player1);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SwordOfTheRealms());
        equipment.setAttachedTo(firstCreature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, secondCreature.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(equipment.getAttachedTo()).isEqualTo(secondCreature.getId());
    }

    @Test
    void castingBackFaceCreatesEquipmentWithItsAbilities() {
        Permanent creature = addCreatureReady(player1);
        harness.setHand(player1, List.of(new HalvarGodOfBattle()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        gs.playCard(gd, player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent equipment = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> gqs.isArtifact(gd, permanent))
                .findFirst()
                .orElseThrow();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int equipmentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(equipment);
        harness.activateAbility(player1, equipmentIndex, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void equippedCreatureReturnsToItsOwnersHandWhenItDies() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SwordOfTheRealms());
        Permanent creature = addCreatureReady(player1);
        equipment.setAttachedTo(creature.getId());
        creature.setToughnessModifier(-2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature.getOriginalCard());
    }

    @Test
    void equipmentGrantsDoubleStrikeOnlyWhileAttachedToAnAlly() {
        harness.addToBattlefield(player1, new HalvarGodOfBattle());
        Permanent ally = addCreatureReady(player1);
        Permanent opponent = addCreatureReady(player2);
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new SwordOfTheRealms());

        assertThat(gqs.hasKeyword(gd, ally, Keyword.DOUBLE_STRIKE)).isFalse();
        equipment.setAttachedTo(ally.getId());
        assertThat(gqs.hasKeyword(gd, ally, Keyword.DOUBLE_STRIKE)).isTrue();
        equipment.setAttachedTo(opponent.getId());
        assertThat(gqs.hasKeyword(gd, ally, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void opponentControlledAuraCanMoveDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new HalvarGodOfBattle());
        Permanent firstCreature = addCreatureReady(player1);
        Permanent secondCreature = addCreatureReady(player1);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new SpiritLink());
        aura.setAttachedTo(firstCreature.getId());

        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.DOUBLE_STRIKE)).isTrue();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.handlePermanentChosen(player1, aura.getId());
        harness.handlePermanentChosen(player1, secondCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(aura.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(aura);
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void mayDeclineMovingEquipment() {
        harness.addToBattlefield(player1, new HalvarGodOfBattle());
        Permanent firstCreature = addCreatureReady(player1);
        Permanent secondCreature = addCreatureReady(player1);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SwordOfTheRealms());
        equipment.setAttachedTo(firstCreature.getId());

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, secondCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(equipment.getAttachedTo()).isEqualTo(firstCreature.getId());
    }

    @Test
    void attachingAuraToItsCurrentCreatureDoesNotTriggerBroodKeeper() {
        harness.addToBattlefield(player1, new HalvarGodOfBattle());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BroodKeeper());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SpiritLink());
        aura.setAttachedTo(creature.getId());

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.handlePermanentChosen(player1, aura.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void swordReturnsOpponentsCreatureToItsOwnerRatherThanEquipmentController() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SwordOfTheRealms());
        Permanent creature = addCreatureReady(player2);
        equipment.setAttachedTo(creature.getId());
        creature.setToughnessModifier(-2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(creature.getOriginalCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature.getOriginalCard());
    }

    @Test
    void swordDoesNotReturnCreatureThatLeftGraveyardBeforeResolution() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SwordOfTheRealms());
        Permanent creature = addCreatureReady(player1);
        equipment.setAttachedTo(creature.getId());
        creature.setToughnessModifier(-2);
        harness.runStateBasedActions();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getOriginalCard());

        gd.playerGraveyards.get(player1.getId()).remove(creature.getOriginalCard());
        gd.playerDecks.get(player1.getId()).add(creature.getOriginalCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature.getOriginalCard());
        assertThat(gd.playerDecks.get(player1.getId())).contains(creature.getOriginalCard());
    }

    @Test
    void frontFaceCanBeCastWithoutAttachmentTargets() {
        harness.castFromHand(player1, new HalvarGodOfBattle(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Halvar, God of Battle");
        harness.assertNotOnBattlefield(player1, "Sword of the Realms");
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
