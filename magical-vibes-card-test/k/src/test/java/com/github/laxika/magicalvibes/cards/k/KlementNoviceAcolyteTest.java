package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KlementNoviceAcolyte.class, GrizzlyBears.class, Plains.class, Island.class,
        Swamp.class, Mountain.class, Forest.class, LightningBolt.class})
class KlementNoviceAcolyteTest extends BaseCardTest {

    @Test
    void perpetuallyBoostsCreatureCardsInHand() {
        GrizzlyBears firstBear = new GrizzlyBears();
        GrizzlyBears secondBear = new GrizzlyBears();
        harness.setHand(player1, List.of(new KlementNoviceAcolyte(), firstBear, secondBear));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2)
                .allSatisfy(permanent -> {
                    assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(3);
                    assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(3);
                });
    }

    @Test
    void entryBoostDoesNotAffectOpponentsHand() {
        harness.setHand(player1, List.of(new KlementNoviceAcolyte()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    void blackSpecializationCreatesTwoZombies() {
        specialize(2, new Swamp());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Klement, Death Acolyte");
        assertThat(findPermanents(player1, "Zombie")).hasSize(2).allSatisfy(zombie -> {
            assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);
        });
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void blueSpecializationChoosesOneOfThreeNonlandPermanents() {
        KlementNoviceAcolyte first = new KlementNoviceAcolyte();
        KlementNoviceAcolyte second = new KlementNoviceAcolyte();
        KlementNoviceAcolyte third = new KlementNoviceAcolyte();
        Plains land = new Plains();
        LightningBolt instant = new LightningBolt();
        harness.setLibrary(player1, List.of(first, second, third, land, instant));
        specialize(1, new Island());
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Klement, Knowledge Acolyte");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(first, third, land, instant);
    }

    @Test
    void blueSpecializationWithOnlyOneEligibleCardKeepsIt() {
        KlementNoviceAcolyte creature = new KlementNoviceAcolyte();
        Plains land = new Plains();
        harness.setLibrary(player1, List.of(land, creature));
        specialize(1, new Island());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void blueSpecializationWithNoEligibleCardsDoesNothing() {
        Plains land = new Plains();
        harness.setLibrary(player1, List.of(land));
        specialize(1, new Island());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void whiteSpecializationUsesTheStackToGrantItsBoon() {
        specialize(0, new Plains());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Klement, Life Acolyte");
    }

    @Test
    void whiteBoonGivesOnlyTheNextCastCreatureALifelinkCounter() {
        specialize(0, new Plains());
        resolveAllTriggers();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent first = findPermanent(player1, "Grizzly Bears");
        assertThat(first.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2)
                .filteredOn(permanent -> permanent != first)
                .allSatisfy(permanent -> assertThat(permanent.getCounterCount(CounterType.LIFELINK)).isZero());
    }

    @Test
    void greenSpecializationCreatesAnOxWhenItDies() {
        specialize(4, new Forest());
        resolveAllTriggers();
        Permanent klement = findPermanent(player1, "Klement, Nature Acolyte");
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, klement.getId());
        harness.castAndResolveInstant(player2, 0, klement.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Klement, Nature Acolyte")).isZero();
        Permanent ox = findPermanent(player1, "Ox");
        assertThat(gqs.getEffectivePower(gd, ox)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ox)).isEqualTo(4);
    }

    @Test
    void redSpecializationReflectsLethalDamageToAnyPlayer() {
        specialize(3, new Mountain());
        resolveAllTriggers();
        Permanent klement = findPermanent(player1, "Klement, Tempest Acolyte");
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, klement.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Klement, Tempest Acolyte")).isZero();
        harness.assertLife(player2, 17);
    }

    @Test
    void specializationRejectsAnUnrelatedDiscardColor() {
        addCreatureReady(player1, new KlementNoviceAcolyte());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void specializationCannotBeActivatedOutsideSorceryTiming() {
        addCreatureReady(player1, new KlementNoviceAcolyte());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 4, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void specializationAcceptsAColoredNonlandCard() {
        specialize(4, new GrizzlyBears());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Klement, Nature Acolyte");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void specializationRequiresACardToDiscard() {
        addCreatureReady(player1, new KlementNoviceAcolyte());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    void blueSpecializationDoesNotRevealTheChosenCard() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        specialize(1, new Island());
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gameLogContains("Grizzly Bears")).isFalse();
    }
    private void specialize(int abilityIndex, Card discard) {
        addCreatureReady(player1, new KlementNoviceAcolyte());
        harness.setHand(player1, List.of(discard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, abilityIndex, null, null);
        if (gd.interaction.isAwaitingInput()) {
            harness.handleCardChosen(player1, 0);
        }
        harness.passBothPriorities();
    }
}
