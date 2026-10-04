package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoonsnareSpecialist;
import com.github.laxika.magicalvibes.cards.w.WebspinnerCuff;
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

@CardUsed({FoundryBeetle.class, GrizzlyBears.class, WebspinnerCuff.class, MoonsnareSpecialist.class})
class FoundryBeetleTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsFirstStrikeAndBeetleStopsBeingACreature() {
        Permanent beetle = addReadyBeetle();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addRedMana();

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(beetle.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, beetle)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        addRedMana();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(beetle.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, beetle)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void upkeepReducesOneRandomArtifactCardInHand() {
        addReadyBeetle();
        harness.setHand(player1, List.of(new WebspinnerCuff(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.passUntil(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Webspinner Cuff"));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void discountsFromMultipleBeetlesAccumulateOnTheOnlyArtifactInHand() {
        addReadyBeetle();
        addReadyBeetle();
        harness.setHand(player1, List.of(new WebspinnerCuff()));

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Webspinner Cuff");
    }

    @Test
    void discountCannotPayColoredManaEvenWhenItExceedsTheGenericCost() {
        addReadyBeetle();
        addReadyBeetle();
        harness.setHand(player1, List.of(new FoundryBeetle()));

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentUpkeepDoesNotDiscountTheControllersHand() {
        addReadyBeetle();
        harness.setHand(player1, List.of(new WebspinnerCuff()));

        advanceToUpkeep(player2);
        resolveAllTriggers();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void upkeepWithNoArtifactsDoesNotDiscountANonartifact() {
        addReadyBeetle();
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent beetle = addReadyBeetle();
        Permanent opponentCreature = addCreatureReady(player2, new WebspinnerCuff());
        addRedMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(beetle.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reconfigureCannotBeActivatedDuringCombat() {
        Permanent beetle = addReadyBeetle();
        Permanent creature = addCreatureReady(player1, new WebspinnerCuff());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        addRedMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(beetle.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reconfiguringToAnotherCreatureMovesTheFirstStrikeGrant() {
        Permanent beetle = addReadyBeetle();
        Permanent firstCreature = addCreatureReady(player1, new WebspinnerCuff());
        Permanent secondCreature = addCreatureReady(player1, new WebspinnerCuff());
        addRedMana();
        harness.activateAbility(player1, 0, 0, null, firstCreature.getId());
        harness.passBothPriorities();

        addRedMana();
        harness.activateAbility(player1, 0, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(beetle.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.isCreature(gd, beetle)).isFalse();
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void perpetualDiscountSurvivesCastingAndReturningTheCardToHand() {
        addReadyBeetle();
        harness.setHand(player1, List.of(new WebspinnerCuff(), new MoonsnareSpecialist()));

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent cuff = findPermanent(player1, "Webspinner Cuff");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0, 0, cuff.getId());
        resolveAllTriggers();
        harness.assertInHand(player1, "Webspinner Cuff");
        harness.assertNotOnBattlefield(player1, "Webspinner Cuff");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Webspinner Cuff");
        harness.assertNotInHand(player1, "Webspinner Cuff");
    }

    private Permanent addReadyBeetle() {
        return addCreatureReady(player1, new FoundryBeetle());
    }

    private void addRedMana() {
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
