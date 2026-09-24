package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordOfLightAndShadow.class, DarksteelGargoyle.class})
class SwordOfLightAndShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2 and protection from white and black")
    void equippedCreatureGetsBoostAndProtection() {
        Permanent creature = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Combat damage gains 3 life and returns a creature card from the graveyard")
    void combatDamageGainsLifeAndReturnsCreature() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new DarksteelGargoyle()));

        Permanent creature = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertInHand(player1, "Darksteel Gargoyle");
        harness.assertNotInGraveyard(player1, "Darksteel Gargoyle");
    }

    @Test
    @DisplayName("Combat damage still gains life when no creature card is in the graveyard")
    void combatDamageGainsLifeWithoutCreatureCard() {
        harness.setLife(player1, 20);

        Permanent creature = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Equip ability attaches Sword to a creature for {2}")
    void equipAbilityAttachesSword() {
        Permanent sword = addSwordReady(player1);
        Permanent creature = addCreatureReady(player1, new DarksteelGargoyle());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Combat damage may decline the optional creature-card return")
    void combatDamageMayDeclineCreatureReturn() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new DarksteelGargoyle()));

        Permanent creature = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertInGraveyard(player1, "Darksteel Gargoyle");
    }

    @Test
    @DisplayName("Combat damage to a creature does not trigger the player-damage ability")
    void combatDamageToCreatureDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new DarksteelGargoyle()));

        Permanent attacker = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new DarksteelGargoyle());

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareAttackersAndPrepareBlockers(List.of(attackerIndex));
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Darksteel Gargoyle");
    }

    private Permanent addSwordReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SwordOfLightAndShadow());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
