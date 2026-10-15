package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({LukaminaMoonDruid.class, Forest.class, GrizzlyBears.class, Shock.class, DoomBlade.class,
        Plains.class, Island.class, Swamp.class, Mountain.class, ActOfTreason.class, Unsummon.class,
        GrafdiggersCage.class})
class LukaminaMoonDruidTest extends BaseCardTest {

    @Test
    void castLukaminaSeeksALandWithABasicLandType() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(new Shock(), forest));
        harness.setHand(player1, List.of(new LukaminaMoonDruid()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(forest.getId()));
    }

    @Test
    void specializesIntoHawkForm() {
        assertThat(specialize(0).getCard().getName()).isEqualTo("Lukamina, Hawk Form");
    }

    @Test
    void specializesIntoCrocodileForm() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(specialize(1, target).getCard().getName()).isEqualTo("Lukamina, Crocodile Form");
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void specializesIntoScorpionForm() {
        assertThat(specialize(2).getCard().getName()).isEqualTo("Lukamina, Scorpion Form");
    }

    @Test
    void specializesIntoWolfForm() {
        assertThat(specialize(3).getCard().getName()).isEqualTo("Lukamina, Wolf Form");
    }

    @Test
    void specializesIntoBearForm() {
        assertThat(specialize(4).getCard().getName()).isEqualTo("Lukamina, Bear Form");
    }

    @Test
    void wolfFormCreatesAWolfWhenItSpecializes() {
        Permanent wolf = specialize(3);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).hasSize(1);
        assertThat(wolf.getCard().getName()).isEqualTo("Lukamina, Wolf Form");
    }

    @Test
    void specializedLukaminaReturnsUnspecializedWhenItDies() {
        Permanent hawk = specialize(0);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, hawk.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Lukamina, Moon Druid");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(hawk.getCard().getId()));
    }

    private Permanent specialize(int colorAbilityIndex) {
        return specialize(colorAbilityIndex, null);
    }

    private Permanent specialize(int colorAbilityIndex, Permanent target) {
        Card discard = switch (colorAbilityIndex) {
            case 0 -> new Plains();
            case 1 -> new Island();
            case 2 -> new Swamp();
            case 3 -> new Mountain();
            case 4 -> new Forest();
            default -> throw new IllegalArgumentException();
        };
        harness.setHand(player1, List.of(discard));
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent lukamina = harness.addToBattlefieldAndReturn(player1, new LukaminaMoonDruid());
        lukamina.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int permanentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(lukamina);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, permanentIndex, colorAbilityIndex, null, null);
        harness.passBothPriorities();
        if (target != null) {
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();
        }
        return lukamina;
    }

    @Test
    void specializeRequiresAnEligibleCardInHand() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent lukamina = harness.addToBattlefieldAndReturn(player1, new LukaminaMoonDruid());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(lukamina), 0, null, null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void enteringWithoutBeingCastDoesNotSeek() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new LukaminaMoonDruid());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void castLukaminaWithNoMatchingLandDoesNotSeekAnotherCard() {
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.setHand(player1, List.of(new LukaminaMoonDruid()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
    }

    @Test
    void hawkFormCombatDamageGainsLife() {
        Permanent hawk = specialize(0);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(hawk)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    void dyingUnspecializesEvenWhenReturningIsProhibited() {
        Permanent hawk = specialize(0);
        harness.addToBattlefield(player2, new GrafdiggersCage());
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, hawk.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(hawk.getCard().getId())
                        && card.getName().equals("Lukamina, Moon Druid"));
        assertThat(findPermanents(player1, "Lukamina, Moon Druid")).isEmpty();
    }

    @Test
    void specializingDiscardsTheEligibleCard() {
        specialize(4);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof Forest);
    }

    @Test
    void cannotSpecializeWithOnlyFiveLands() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent lukamina = harness.addToBattlefieldAndReturn(player1, new LukaminaMoonDruid());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(lukamina), 4, null, null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void bearFormBoostsOnlyOtherFriendlyCreatures() {
        Permanent friendly = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent bear = specialize(4);

        assertThat(gqs.getEffectivePower(gd, friendly)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, friendly)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, friendly, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
    }

    @Test
    void wolfFormCreatesAnotherWolfWhenItAttacks() {
        Permanent wolf = specialize(3);
        resolveAllTriggers();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(wolf)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).hasSize(2);
    }

    @Test
    void crocodileLockEndsWhenItsControllerLosesControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent crocodile = specialize(1, target);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ActOfTreason()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player2, 0, crocodile.getId());

        harness.performUntapStep(player2);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void castingCrocodileFormDoesNotTriggerItsSpecializationAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent crocodile = specialize(1, target);
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, crocodile.getId());
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }
}
