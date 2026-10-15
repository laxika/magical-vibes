package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MuragandaPetroglyphs;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OkoLorwynLiege.class, OkoShadowmoorScion.class, Forest.class,
        GrizzlyBears.class, LlanowarElves.class, Shock.class, MuragandaPetroglyphs.class})
class OkoLorwynLiegeTest extends BaseCardTest {

    @Test
    void frontFaceTransformsAfterPayingGreenInFirstMainPhase() {
        Permanent oko = addFrontFace(player1, 3);
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(oko.isTransformed()).isTrue();
    }

    @Test
    void backFaceTransformsAfterPayingBlueInFirstMainPhase() {
        Permanent oko = addBackFace(player1, 3);
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(oko.isTransformed()).isFalse();
    }

    @Test
    void frontFaceLoyaltyAbilitiesWork() {
        Permanent oko = addFrontFace(player1, 3);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        int okoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(oko);
        harness.activateAbility(player1, okoIndex, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(oko.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.ELK)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.CHANGELING)).isFalse();

        int powerBeforeMinusOne = gqs.getEffectivePower(gd, target);
        oko.setLoyaltyActivationsThisTurn(0);
        harness.activateAbility(player1, okoIndex, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(powerBeforeMinusOne - 2);
    }

    @Test
    void millsAndOffersOnlyPermanentCards() {
        Forest forest = new Forest();
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, shock, bears));
        Permanent oko = addBackFace(player1, 3);

        int okoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(oko);
        harness.activateAbility(player1, okoIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(bears, shock)
                .doesNotContain(forest);
    }

    @Test
    void createsTwoElkTokens() {
        Permanent oko = addBackFace(player1, 3);

        int okoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(oko);
        harness.activateAbility(player1, okoIndex, 1, null, null);
        harness.passBothPriorities();

        List<Permanent> elks = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ELK))
                .toList();
        assertThat(elks).hasSize(2);
        assertThat(elks).allSatisfy(elk -> {
            assertThat(elk.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(elk.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(elk.getEffectivePower()).isEqualTo(3);
            assertThat(elk.getEffectiveToughness()).isEqualTo(3);
        });
    }

    @Test
    void emblemBoostsChosenCreatureType() {
        Permanent oko = addBackFace(player1, 6);
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        int elfPowerBefore = gqs.getEffectivePower(gd, elf);
        int elfToughnessBefore = gqs.getEffectiveToughness(gd, elf);
        int bearPowerBefore = gqs.getEffectivePower(gd, bear);

        int okoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(oko);
        harness.activateAbility(player1, okoIndex, 2, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        assertThat(gd.emblems).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(elfPowerBefore + 3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(elfToughnessBefore + 3);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(bearPowerBefore);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void gainingCreatureTypesDoesNotRemoveBonusForHavingNoAbilities() {
        Permanent oko = addFrontFace(player1, 3);
        harness.addToBattlefield(player1, new MuragandaPetroglyphs());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        int powerBefore = gqs.getEffectivePower(gd, bear);
        int toughnessBefore = gqs.getEffectiveToughness(gd, bear);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(oko),
                0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.ELF)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(toughnessBefore);
    }

    @Test
    void plusTwoCanBeActivatedWithoutATarget() {
        Permanent oko = addFrontFace(player1, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(oko.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void frontFaceCanDeclineTransformAndKeepItsLoyalty() {
        Permanent oko = addFrontFace(player1, 5);
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(oko.isTransformed()).isFalse();
        assertThat(oko.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void transformPreservesExistingLoyalty() {
        Permanent oko = addFrontFace(player1, 5);
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(oko.isTransformed()).isTrue();
        assertThat(oko.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void backFaceCanDeclineTransform() {
        Permanent oko = addBackFace(player1, 4);
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(oko.isTransformed()).isTrue();
        assertThat(oko.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void doesNotOfferTransformDuringOpponentsFirstMainPhase() {
        Permanent oko = addFrontFace(player1, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        advanceToPrecombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(oko.isTransformed()).isFalse();
    }

    @Test
    void millingOnlyInstantsDoesNotOfferReturnToHand() {
        Shock first = new Shock();
        Shock second = new Shock();
        Shock third = new Shock();
        harness.setLibrary(player1, List.of(first, second, third));
        addBackFace(player1, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second, third);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second, third);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canDeclineFirstMilledPermanentAndTakeTheSecond() {
        Forest forest = new Forest();
        GrizzlyBears bear = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(forest, shock, bear));
        addBackFace(player1, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(bear).doesNotContain(forest, shock);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, shock).doesNotContain(bear);
    }

    @Test
    void canDeclineAllMilledPermanentsWithAShortLibrary() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        addBackFace(player1, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    void powerReductionEndsOnlyAtAbilityControllersNextTurn() {
        Permanent oko = addFrontFace(player1, 3);
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        int powerBefore = gqs.getEffectivePower(gd, bear);

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(powerBefore - 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(powerBefore - 2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(powerBefore);
    }

    @Test
    void gainedCreatureTypesSurviveSourceLeavingAndTurnCleanup() {
        Permanent oko = addFrontFace(player1, 3);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(oko);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.ELK)).isTrue();
    }

    @Test
    void emblemAppliesToLaterCreaturesButNotOpponentsCreatures() {
        addBackFace(player1, 6);
        Permanent opposingElf = addCreatureReady(player2, new LlanowarElves());
        int opposingPowerBefore = gqs.getEffectivePower(gd, opposingElf);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        Permanent ownElf = addCreatureReady(player1, new LlanowarElves());
        assertThat(gqs.getEffectivePower(gd, ownElf)).isEqualTo(opposingPowerBefore + 3);
        assertThat(gqs.getEffectivePower(gd, opposingElf)).isEqualTo(opposingPowerBefore);
        assertThat(gqs.hasKeyword(gd, ownElf, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownElf, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingElf, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingElf, Keyword.HEXPROOF)).isFalse();
    }

    private Permanent addFrontFace(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new OkoLorwynLiege());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addBackFace(Player player, int loyalty) {
        Permanent permanent = addFrontFace(player, loyalty);
        permanent.setTransformed(true);
        permanent.setCard(permanent.getOriginalCard().getBackFaceCard());
        return permanent;
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
