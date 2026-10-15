package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.s.SealOfRemoval;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JaheiraHarperEmissary.class, Forest.class, GrizzlyBears.class, Island.class,
        MindStone.class, Mountain.class, Plains.class, Swamp.class, IcyManipulator.class,
        Pacifism.class, Unsummon.class, SealOfRemoval.class})
class JaheiraHarperEmissaryTest extends BaseCardTest {

    @Test
    void whiteFaceDestroysAnArtifactAndCountersOtherCreatures() {
        Permanent jaheira = addCreatureReady(player1, new JaheiraHarperEmissary());
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());

        activateSpecialization(jaheira, 0, new Plains());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(artifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.handlePermanentChosen(player1, firstCreature.getId());
        harness.handlePermanentChosen(player1, secondCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mind Stone");
        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void blueFaceScriesTwoWhenThereIsNoArtifactOrEnchantment() {
        Permanent jaheira = addCreatureReady(player1, new JaheiraHarperEmissary());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Island(), new Forest()));

        activateSpecialization(jaheira, 1, new Island());
        skipOptionalArtifactTarget();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    void blackFaceMakesEachOpponentLoseThreeLife() {
        Permanent jaheira = addCreatureReady(player1, new JaheiraHarperEmissary());

        activateSpecialization(jaheira, 2, new Swamp());
        skipOptionalArtifactTarget();
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void greenFaceGainsFourLife() {
        Permanent jaheira = addCreatureReady(player1, new JaheiraHarperEmissary());
        harness.setLife(player1, 10);

        activateSpecialization(jaheira, 4, new Forest());
        skipOptionalArtifactTarget();
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
    }

    @Test
    void redFaceGivesTheNextCreatureSpellAPerpetualPowerBoostAndHaste() {
        Permanent jaheira = addCreatureReady(player1, new JaheiraHarperEmissary());

        activateSpecialization(jaheira, 3, new Mountain());
        skipOptionalArtifactTarget();
        harness.passBothPriorities();

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent bearPermanent = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bearPermanent)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bearPermanent)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bearPermanent, Keyword.HASTE)).isTrue();
    }

    @Test
    void specializationRejectsDiscardingAnUnrelatedBasicLand() {
        addCreatureReady(player1, new JaheiraHarperEmissary());
        harness.setHand(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 4, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Island");
        harness.assertOnBattlefield(player1, "Jaheira, Harper Emissary");
    }

    @Test
    void specializationCannotBeActivatedDuringTheEndStep() {
        addCreatureReady(player1, new JaheiraHarperEmissary());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 4, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void opposingAuraCannotTargetJaheira() {
        Permanent jaheira = addCreatureReady(player1, new JaheiraHarperEmissary());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Pacifism()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, jaheira.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ownAuraCanTargetJaheira() {
        Permanent jaheira = addCreatureReady(player1, new JaheiraHarperEmissary());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, jaheira.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pacifism");
        assertThat(findPermanent(player1, "Pacifism").getAttachedTo()).isEqualTo(jaheira.getId());
    }

    @Test
    void opposingArtifactAbilityCannotTargetJaheira() {
        Permanent jaheira = addCreatureReady(player1, new JaheiraHarperEmissary());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, jaheira.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opposingEnchantmentAbilityCannotTargetJaheira() {
        Permanent jaheira = addCreatureReady(player1, new JaheiraHarperEmissary());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SealOfRemoval());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, jaheira.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Seal of Removal");
    }

    @Test
    void specializedFaceRetainsHexproofFromOpposingEnchantments() {
        Permanent jaheira = addCreatureReady(player1, new JaheiraHarperEmissary());
        activateSpecialization(jaheira, 4, new Forest());
        skipOptionalArtifactTarget();
        harness.passBothPriorities();
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Pacifism()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, jaheira.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void specializedJaheiraCanBeReturnedByAnOpposingInstantAndRecastWithoutSpecializingAgain() {
        Permanent jaheira = addCreatureReady(player1, new JaheiraHarperEmissary());
        activateSpecialization(jaheira, 4, new GrizzlyBears());
        skipOptionalArtifactTarget();
        harness.passBothPriorities();
        harness.assertLife(player1, 24);

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, jaheira.getId());
        harness.assertInHand(player1, "Jaheira, Merciful Harper");

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Jaheira, Merciful Harper");
        harness.assertLife(player1, 24);
    }

    @Test
    void whiteFaceCanSkipDestructionAndChooseOnlyOneOtherCreature() {
        Permanent jaheira = addCreatureReady(player1, new JaheiraHarperEmissary());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new MindStone());

        activateSpecialization(jaheira, 0, new Plains());
        skipOptionalArtifactTarget();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(creature.getId()).doesNotContain(jaheira.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        skipOptionalArtifactTarget();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mind Stone");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(jaheira.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void greenFaceDoesNotGainLifeWhenItsOnlyTargetBecomesIllegal() {
        Permanent jaheira = addCreatureReady(player1, new JaheiraHarperEmissary());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setLibrary(player2, List.of(new Forest()));

        activateSpecialization(jaheira, 4, new Forest());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, 1, null, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Mind Stone");
        harness.assertLife(player1, 20);
    }

    @Test
    void greenFaceDestroysAnEnchantmentAndGainsLife() {
        Permanent jaheira = addCreatureReady(player1, new JaheiraHarperEmissary());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Pacifism()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player2, 0, creature.getId());
        harness.passBothPriorities();
        Permanent enchantment = findPermanent(player2, "Pacifism");

        activateSpecialization(jaheira, 4, new Forest());
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Pacifism");
        harness.assertLife(player1, 24);
    }

    @Test
    void redBoonIsConsumedOnlyOnceAndItsBonusSurvivesReturningToHand() {
        Permanent jaheira = addCreatureReady(player1, new JaheiraHarperEmissary());
        activateSpecialization(jaheira, 3, new Mountain());
        skipOptionalArtifactTarget();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent first = findPermanent(player1, "Grizzly Bears");

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, first.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent second = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .filter(p -> !p.getId().equals(returned.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isFalse();
    }

    private void activateSpecialization(Permanent jaheira, int abilityIndex,
                                        com.github.laxika.magicalvibes.model.Card discarded) {
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(jaheira),
                abilityIndex, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }

    private void skipOptionalArtifactTarget() {
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        if (choice != null) {
            harness.handlePermanentChosen(player1, player1.getId());
        }
    }

}
