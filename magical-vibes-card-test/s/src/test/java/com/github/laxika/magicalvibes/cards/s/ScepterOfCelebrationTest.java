package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScepterOfCelebration.class, GrizzlyBears.class})
class ScepterOfCelebrationTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0 and trample")
    void equippedCreatureGetsBoostAndTrample() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scepter = addScepterReady(player1);
        scepter.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature creates Citizens equal to combat damage dealt to a player")
    void combatDamageToPlayerCreatesCitizensEqualToDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scepter = addScepterReady(player1);
        scepter.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Citizen")).hasSize(4);
        assertThat(findPermanents(player1, "Citizen")).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.CITIZEN);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Trample creates Citizens only for damage dealt to the player")
    void trampleCreatesCitizensOnlyForPlayerDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scepter = addScepterReady(player1);
        scepter.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2, player2.getId(), 2));
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(findPermanents(player1, "Citizen")).hasSize(2);
    }

    @Test
    @DisplayName("Damage absorbed by blockers creates no Citizens")
    void combatDamageToCreaturesDoesNotCreateCitizens() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scepter = addScepterReady(player1);
        scepter.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());
        firstBlocker.setBlocking(true);
        firstBlocker.addBlockingTarget(0);
        secondBlocker.setBlocking(true);
        secondBlocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(firstBlocker.getId(), 2, secondBlocker.getId(), 2));
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Citizen")).isEmpty();
    }

    @Test
    @DisplayName("Equip attaches to a controlled creature for three mana")
    void equipAttachesToCreature() {
        Permanent scepter = addScepterReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(scepter.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The Equipment controller creates Citizens when another player controls the creature")
    void equipmentControllerCreatesTokens() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scepter = addScepterReady(player2);
        scepter.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Citizen")).isEmpty();
        assertThat(findPermanents(player2, "Citizen")).hasSize(4);
    }

    private Permanent addScepterReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ScepterOfCelebration());
    }
}
