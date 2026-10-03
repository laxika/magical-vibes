package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HourOfDefeat;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.s.Shatterstorm;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaptainAmericasShield.class, GrizzlyBears.class, HourOfDefeat.class,
        MarchOfTheMachines.class, Shatterstorm.class})
class CaptainAmericasShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +0/+8 and vigilance")
    void equippedCreatureGetsBoostAndVigilance() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(10);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Attacking with the equipped creature taps a creature defending player controls")
    void attackingTapsDefendingCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(attacker.getId());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The attack trigger only targets creatures controlled by the defending player")
    void attackTriggerOnlyTargetsDefendingCreatures() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(attacker.getId());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(defendingCreature.getId())
                .doesNotContain(attacker.getId(), ownCreature.getId());
    }

    @Test
    void equipTransfersBoostAndVigilanceOnlyOnResolution() {
        Permanent shield = addShieldReady(player1);
        Permanent original = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        shield.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(shield.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, original, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(10);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent shield = addShieldReady(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(shield.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipDuringCombat() {
        Permanent shield = addShieldReady(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(shield.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void shieldSurvivesArtifactDestruction() {
        Permanent shield = addShieldReady(player1);
        harness.setHand(player1, List.of(new Shatterstorm()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, (java.util.UUID) null);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shield);
    }

    @Test
    void equippedCreatureIsStillDestroyedByDestroyCreatureSpell() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new HourOfDefeat()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature).contains(shield);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
        assertThat(shield.getAttachedTo()).isNull();
    }

    @Test
    void unattachedShieldDoesNotTriggerWhenCreatureAttacks() {
        addCreatureReady(player1, new GrizzlyBears());
        addShieldReady(player1);
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(defender.isTapped()).isFalse();
    }

    @Test
    void attackingWithAnimatedShieldDoesNotTriggerEquippedCreatureAbility() {
        addShieldReady(player1);
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(defender.isTapped()).isFalse();
    }

    @Test
    void attackTriggerResolvesAfterShieldLeavesBattlefield() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(attacker.getId());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        gd.playerBattlefields.get(player1.getId()).remove(shield);
        gd.playerHands.get(player1.getId()).add(shield.getCard());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.VIGILANCE)).isFalse();
    }

    private Permanent addShieldReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new CaptainAmericasShield());
    }
}
