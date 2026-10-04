package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EnsouledScimitar;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.v.VulshokSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinBrawler.class, EnsouledScimitar.class, VulshokSorcerer.class, Humble.class})
class GoblinBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Equip may target Goblin Brawler but the Equipment does not attach")
    void cannotBeEquipped() {
        Permanent brawler = addCreatureReady(player1, new GoblinBrawler());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new EnsouledScimitar());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, 1, null, brawler.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("An Equipment already attached to Goblin Brawler becomes unattached")
    void alreadyAttachedEquipmentBecomesUnattached() {
        Permanent brawler = addCreatureReady(player1, new GoblinBrawler());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new EnsouledScimitar());
        scimitar.setAttachedTo(brawler.getId());

        harness.runStateBasedActions();

        assertThat(scimitar.getAttachedTo()).isNull();
    }
    @Test
    @DisplayName("A failed equip attempt leaves Equipment attached to its previous creature")
    void failedEquipPreservesPreviousAttachment() {
        Permanent brawler = addCreatureReady(player1, new GoblinBrawler());
        Permanent sorcerer = addCreatureReady(player1, new VulshokSorcerer());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new EnsouledScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 2, 1, null, sorcerer.getId());
        harness.passBothPriorities();
        assertThat(scimitar.getAttachedTo()).isEqualTo(sorcerer.getId());

        harness.activateAbility(player1, 2, 1, null, brawler.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(sorcerer.getId());
    }

    @Test
    @DisplayName("Goblin Brawler can be equipped after losing all abilities")
    void canBeEquippedAfterLosingAbilities() {
        Permanent brawler = addCreatureReady(player1, new GoblinBrawler());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new EnsouledScimitar());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, brawler.getId());
        harness.activateAbility(player1, 1, 1, null, brawler.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(brawler.getId());
        harness.runStateBasedActions();
        assertThat(scimitar.getAttachedTo()).isEqualTo(brawler.getId());
    }

    @Test
    @DisplayName("First strike kills a blocker before it can deal combat damage")
    void firstStrikeKillsBlockerBeforeItDealsDamage() {
        Permanent brawler = addCreatureReady(player1, new GoblinBrawler());
        addCreatureReady(player2, new VulshokSorcerer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Goblin Brawler");
        harness.assertInGraveyard(player2, "Vulshok Sorcerer");
        assertThat(brawler.getMarkedDamage()).isZero();
    }
}
