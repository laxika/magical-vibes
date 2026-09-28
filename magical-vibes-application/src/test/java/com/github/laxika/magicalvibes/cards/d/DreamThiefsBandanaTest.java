package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent addReadyBandana() {
        Permanent bandana = harness.addToBattlefieldAndReturn(player1, new DreamThiefsBandana());
        bandana.setSummoningSick(false);
        return bandana;
    }
}
