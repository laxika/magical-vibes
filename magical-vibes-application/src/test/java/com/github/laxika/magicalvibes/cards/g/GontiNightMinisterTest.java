package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GontiNightMinisterTest extends BaseCardTest {

    @Test
    @DisplayName("A player casting a spell they do not own creates a Treasure for that player")
    void unownedSpellCreatesTreasureForCaster() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new GontiNightMinister());
        harness.setLibrary(player2, List.of(new Island(), new Island()));

        Divination spell = new Divination();
        spell.setOwnerId(player1.getId());
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Treasure");
    }

    @Test
    @DisplayName("Combat damage to an opponent exiles the top card face down with indefinite play permission")
    void combatDamageExilesFaceDownAndGrantsIndefinitePermission() {
        Permanent gonti = harness.addToBattlefieldAndReturn(player1, new GontiNightMinister());
        addAttackingCreature(player1, new GrizzlyBears());
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard, new Island()));

        resolveCombatAndTrigger();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(entry.ownerId()).isEqualTo(player2.getId());
        assertThat(entry.sourcePermanentId()).isEqualTo(gonti.getId());
        assertThat(entry.exilerId()).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(topCard.getId());
    }

    @Test
    @DisplayName("The play permission remains after Gonti leaves the battlefield")
    void permissionPersistsAfterSourceLeaves() {
        Permanent gonti = harness.addToBattlefieldAndReturn(player1, new GontiNightMinister());
        addAttackingCreature(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();
        gd.playerBattlefields.get(player1.getId()).remove(gonti);

        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    @DisplayName("The creature controller can cast the exiled spell using mana of any type")
    void creatureControllerCanCastExiledSpellWithAnyMana() {
        harness.addToBattlefield(player1, new GontiNightMinister());
        addAttackingCreature(player1, new GrizzlyBears());
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard, new Island(), new Island()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        int handSizeBeforeCast = gd.playerHands.get(player1.getId()).size();

        resolveCombatAndTrigger();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeCast + 2);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    private Permanent addAttackingCreature(Player player, Card card) {
        Permanent creature = addCreatureReady(player, card);
        creature.setAttacking(true);
        return creature;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
