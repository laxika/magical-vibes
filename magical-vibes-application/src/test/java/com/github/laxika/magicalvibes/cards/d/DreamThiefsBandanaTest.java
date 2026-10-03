package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreamThiefsBandana.class, GrizzlyBears.class, Island.class})
class DreamThiefsBandanaTest extends BaseCardTest {

    @Test
    void equipAbilityAttachesBandanaForOneMana() {
        Permanent bandana = addReadyBandana();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(bandana.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void combatDamageExilesDamagedPlayersTopCardFaceDownWithPersistentAnyManaPermission() {
        Permanent bandana = addReadyBandana();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        bandana.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();
        harness.passBothPriorities();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(entry.ownerId()).isEqualTo(player2.getId());
        assertThat(entry.sourcePermanentId()).isEqualTo(bandana.getId());
        assertThat(entry.exilerId()).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(topCard.getId());
    }

    @Test
    void permissionRemainsAfterBandanaLeavesTheBattlefield() {
        Permanent bandana = addReadyBandana();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        bandana.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(bandana);

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    void exiledCreatureCanBeCastWithColorlessManaAfterBandanaLeaves() {
        Permanent bandana = addReadyBandana();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        bandana.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        Card stolenCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(stolenCard));

        resolveCombat();
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(bandana);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        assertThatThrownBy(() -> harness.castFromExile(player1, stolenCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(stolenCard.getId())).isNotNull();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromExile(player1, stolenCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(stolenCard.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(stolenCard.getId()));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(stolenCard.getId());
    }

    @Test
    void exiledLandCanBePlayedDuringMainPhase() {
        Permanent bandana = addReadyBandana();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        bandana.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        Card stolenCard = new Island();
        harness.setLibrary(player2, List.of(stolenCard));

        resolveCombat();
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromExile(player1, stolenCard.getId());

        assertThat(gd.findExiledCard(stolenCard.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(stolenCard.getId()));
    }

    @Test
    void equipmentControllerGetsPermissionWhenOpponentsCreatureIsEquipped() {
        Permanent bandana = addReadyBandana();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        bandana.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.findExiledCard(topCard.getId()).exilerId()).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    void emptyLibraryDoesNotExileAnything() {
        Permanent bandana = addReadyBandana();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        bandana.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        harness.setLibrary(player2, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    void unequippedBandanaDoesNotTriggerForCombatDamage() {
        addReadyBandana();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.exiledCards).isEmpty();
    }

    private Permanent addReadyBandana() {
        Permanent bandana = harness.addToBattlefieldAndReturn(player1, new DreamThiefsBandana());
        bandana.setSummoningSick(false);
        return bandana;
    }
}
