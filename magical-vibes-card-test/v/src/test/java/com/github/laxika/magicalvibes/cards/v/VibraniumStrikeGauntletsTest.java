package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VibraniumStrikeGauntlets.class, GrizzlyBears.class, Forest.class})
class VibraniumStrikeGauntletsTest extends BaseCardTest {

    @Test
    void entersAttachedToTargetCreatureAndGrantsAbilities() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VibraniumStrikeGauntlets()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0, creature.getId());
        resolveAllTriggers();

        Permanent gauntlets = findPermanent(player1, "Vibranium Strike Gauntlets");
        assertThat(gauntlets.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void equippedCreatureDealsCombatDamageAndDraws() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setAttacking(true);
        Permanent gauntlets = harness.addToBattlefieldAndReturn(player1, new VibraniumStrikeGauntlets());
        gauntlets.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof Forest);
    }

    @Test
    void entersAbilityCannotTargetAnOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VibraniumStrikeGauntlets()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void equipMovesAbilitiesToAnotherCreature() {
        Permanent gauntlets = harness.addToBattlefieldAndReturn(player1, new VibraniumStrikeGauntlets());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        gauntlets.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, second.getId());
        resolveAllTriggers();

        assertThat(gauntlets.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
        first.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void canCastDuringOpponentsCombat() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.ensurePriority(player1);
        harness.setHand(player1, List.of(new VibraniumStrikeGauntlets()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Vibranium Strike Gauntlets").getAttachedTo())
                .isEqualTo(creature.getId());
    }

    @Test
    void canEnterWithoutAnyCreatureToAttachTo() {
        harness.setHand(player1, List.of(new VibraniumStrikeGauntlets()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Vibranium Strike Gauntlets").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureControllerDrawsWhenEquipmentHasADifferentController() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.setAttacking(true);
        Permanent gauntlets = harness.addToBattlefieldAndReturn(player1, new VibraniumStrikeGauntlets());
        gauntlets.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
