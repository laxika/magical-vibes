package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Glamdring.class, GrizzlyBears.class, GiantGrowth.class, Shock.class,
        CounselOfTheSoratami.class, Forest.class})
class GlamdringTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets first strike and +1/+0 for each instant or sorcery in Glamdring's controller's graveyard")
    void equippedCreatureGetsGraveyardScalingBoostAndFirstStrike() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent glamdring = harness.addToBattlefieldAndReturn(player1, new Glamdring());
        glamdring.setAttachedTo(creature.getId());
        harness.setGraveyard(player1, List.of(new Shock(), new GiantGrowth(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Combat damage offers an instant or sorcery from hand with mana value at most the damage dealt")
    void combatDamageOffersEligibleFreeCast() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent glamdring = harness.addToBattlefieldAndReturn(player1, new Glamdring());
        glamdring.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Shock eligible = new Shock();
        CounselOfTheSoratami tooExpensive = new CounselOfTheSoratami();
        Forest land = new Forest();
        harness.setHand(player1, List.of(eligible, tooExpensive, land));

        resolveCombat();
        resolveAllTriggers();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.description()).contains("Shock").doesNotContain("Counsel of the Soratami");

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(eligible.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(tooExpensive, land);
    }

    @Test
    @DisplayName("Glamdring does not trigger when the equipped creature deals combat damage only to a creature")
    void blockedCreatureDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent glamdring = harness.addToBattlefieldAndReturn(player1, new Glamdring());
        glamdring.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        harness.setHand(player1, List.of(new Shock()));

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void equipPaysThreeManaAndMovesTheGrantedAbilities() {
        Permanent glamdring = harness.addToBattlefieldAndReturn(player1, new Glamdring());
        Permanent original = addCreatureReady(player1, new GrizzlyBears());
        Permanent replacement = addCreatureReady(player1, new GrizzlyBears());
        glamdring.setAttachedTo(original.getId());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, replacement.getId());

        assertThat(glamdring.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(glamdring.getAttachedTo()).isEqualTo(replacement.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, original, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void boostUsesEquipmentControllersGraveyardAndUpdatesContinuously() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent glamdring = harness.addToBattlefieldAndReturn(player1, new Glamdring());
        glamdring.setAttachedTo(creature.getId());
        harness.setGraveyard(player1, List.of(new Shock(), new CounselOfTheSoratami(), new Forest()));
        harness.setGraveyard(player2, List.of(new GiantGrowth()));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        harness.setGraveyard(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void mayCastSorceryWithManaValueEqualToCombatDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent glamdring = harness.addToBattlefieldAndReturn(player1, new Glamdring());
        glamdring.setAttachedTo(creature.getId());
        harness.setGraveyard(player1, List.of(new Shock()));
        creature.setAttacking(true);
        CounselOfTheSoratami sorcery = new CounselOfTheSoratami();
        Shock otherSpell = new Shock();
        harness.setHand(player1, List.of(sorcery, otherSpell));
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .description()).contains("Counsel of the Soratami");

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(sorcery.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(otherSpell);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void decliningFreeCastLeavesSpellInHand() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent glamdring = harness.addToBattlefieldAndReturn(player1, new Glamdring());
        glamdring.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Shock spell = new Shock();
        harness.setHand(player1, List.of(spell));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void ineligibleHandCardsAreNotOffered() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent glamdring = harness.addToBattlefieldAndReturn(player1, new Glamdring());
        glamdring.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        CounselOfTheSoratami expensive = new CounselOfTheSoratami();
        GrizzlyBears creatureCard = new GrizzlyBears();
        Forest land = new Forest();
        harness.setHand(player1, List.of(expensive, creatureCard, land));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(expensive, creatureCard, land);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipmentControllerGetsFreeCastWhenOpponentControlsEquippedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent glamdring = harness.addToBattlefieldAndReturn(player1, new Glamdring());
        glamdring.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.setAttackTarget(player1.getId());
        Shock spell = new Shock();
        harness.setHand(player1, List.of(spell));
        harness.setHand(player2, List.of(new GiantGrowth()));

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .description()).contains("Shock");
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
