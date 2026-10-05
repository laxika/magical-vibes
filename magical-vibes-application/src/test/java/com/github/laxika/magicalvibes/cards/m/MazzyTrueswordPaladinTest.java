package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MazzyTrueswordPaladin.class, GrizzlyBears.class, HolyStrength.class,
        ChandraNalaar.class, Naturalize.class, ShortSword.class, PlanarCleansing.class})
class MazzyTrueswordPaladinTest extends BaseCardTest {

    @Test
    void boostsAnEnchantedCreatureAttackingAnOpponent() {
        addCreatureReady(player1, new MazzyTrueswordPaladin());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(attacker.getId());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(attacker.getToughnessModifier()).isZero();
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void doesNotBoostAnEnchantedCreatureAttackingAnOpponentsPlaneswalker() {
        addCreatureReady(player1, new MazzyTrueswordPaladin());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(attacker.getId());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareAttackers(player1, List.of(attackerIndex), Map.of(attackerIndex, planeswalker.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void exilesDestroyedAuraAndAllowsCastingItUntilNextTurn() {
        addCreatureReady(player1, new MazzyTrueswordPaladin());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(attacker.getId());

        destroyWithNaturalize(aura);

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(aura.getCard());
        assertThat(gd.exilePlayPermissions).containsEntry(aura.getCard().getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).containsKey(aura.getCard().getId());
    }

    @Test
    void destroyedEquipmentStaysInTheGraveyard() {
        addCreatureReady(player1, new MazzyTrueswordPaladin());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new ShortSword());
        equipment.setAttachedTo(attacker.getId());

        destroyWithNaturalize(equipment);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(equipment.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(equipment.getCard());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(equipment.getCard().getId());
    }

    @Test
    void doesNotBoostAnUnenchantedAttacker() {
        addCreatureReady(player1, new MazzyTrueswordPaladin());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void doesNotBoostAnEnchantedCreatureAttackingMazzysController() {
        addCreatureReady(player1, new MazzyTrueswordPaladin());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        aura.setAttachedTo(attacker.getId());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void doesNotExileAControlledAuraThatGoesToItsOpponentsGraveyard() {
        addCreatureReady(player1, new MazzyTrueswordPaladin());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(creature.getId());
        gd.stolenCreatures.put(aura.getId(), player2.getId());

        destroyWithNaturalize(aura);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(aura.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(aura.getCard());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(aura.getCard().getId());
    }

    @Test
    void canCastTheExiledAuraByPayingItsManaCost() {
        addCreatureReady(player1, new MazzyTrueswordPaladin());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(creature.getId());
        destroyWithNaturalize(aura);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, aura.getCard().getId(), creature.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(aura.getCard());
        assertThat(findPermanent(player1, "Holy Strength").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(aura.getCard().getId());
    }

    @Test
    void exilesAnAuraDestroyedSimultaneouslyWithMazzy() {
        addCreatureReady(player1, new MazzyTrueswordPaladin());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(aura.getCard());
        assertThat(gd.exilePlayPermissions).containsEntry(aura.getCard().getId(), player1.getId());
    }

    @Test
    void castPermissionLastsThroughTheNextTurnAndThenExpires() {
        addCreatureReady(player1, new MazzyTrueswordPaladin());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(creature.getId());
        destroyWithNaturalize(aura);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(aura.getCard().getId(), player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(aura.getCard().getId(), player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(aura.getCard().getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(aura.getCard());
    }

    private void destroyWithNaturalize(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices,
                                  Map<Integer, java.util.UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }
}
