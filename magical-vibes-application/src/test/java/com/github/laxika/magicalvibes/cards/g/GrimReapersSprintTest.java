package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.InfestingRadroach;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrimReapersSprint.class, InfestingRadroach.class})
class GrimReapersSprintTest extends BaseCardTest {

    @Test
    @DisplayName("Morbid reduces the Aura's casting cost by three")
    void morbidReducesCastingCost() {
        Permanent creature = addCreatureReady(player2, new InfestingRadroach());
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);
        harness.setHand(player1, List.of(new GrimReapersSprint()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Without morbid, the Aura needs its full mana cost")
    void needsFullManaCostWithoutMorbid() {
        Permanent creature = addCreatureReady(player2, new InfestingRadroach());
        harness.setHand(player1, List.of(new GrimReapersSprint()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2 and haste")
    void enchantedCreatureGetsBoostAndHaste() {
        Permanent creature = addCreatureReady(player1, new InfestingRadroach());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GrimReapersSprint());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Entering during a main phase untaps creatures and adds a combat phase")
    void enteringDuringMainPhaseUntapsCreaturesAndAddsCombat() {
        Permanent target = addCreatureReady(player1, new InfestingRadroach());
        Permanent otherCreature = addCreatureReady(player1, new InfestingRadroach());
        otherCreature.tap();

        harness.setHand(player1, List.of(new GrimReapersSprint()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(otherCreature.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    void opponentsCreatureDeathEnablesMorbid() {
        Permanent target = addCreatureReady(player1, new InfestingRadroach());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new GrimReapersSprint()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void enchantingOpponentsCreatureUntapsOnlyYourCreatures() {
        Permanent target = addCreatureReady(player2, new InfestingRadroach());
        Permanent ownCreature = addCreatureReady(player1, new InfestingRadroach());
        target.tap();
        ownCreature.tap();
        harness.setHand(player1, List.of(new GrimReapersSprint()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    void enteringDuringOpponentsMainPhaseUntapsWithoutAddingCombat() {
        Permanent target = addCreatureReady(player1, new InfestingRadroach());
        target.tap();
        harness.setHand(player1, List.of(new GrimReapersSprint()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0, target.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        resolveAllTriggers();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    @Test
    void postcombatMainPhaseIsFollowedByAdditionalCombat() {
        Permanent target = addCreatureReady(player1, new InfestingRadroach());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrimReapersSprint()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();

        harness.withAutoStop(TurnStep.END_STEP, () ->
                harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, harness::passBothPriorities));

        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
    }
}
