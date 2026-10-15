package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CookingCampsite;
import com.github.laxika.magicalvibes.cards.d.DwarvenCastleGuard;
import com.github.laxika.magicalvibes.cards.e.EsperTerra;
import com.github.laxika.magicalvibes.cards.f.FireMagic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SidequestCatchAFish;
import com.github.laxika.magicalvibes.cards.s.SummonGFIfrit;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TerraMagicalAdept.class, EsperTerra.class, SidequestCatchAFish.class,
        CookingCampsite.class, Forest.class, DwarvenCastleGuard.class, SummonGFIfrit.class, FireMagic.class})
class TerraMagicalAdeptTest extends BaseCardTest {

    @Test
    void entersByMillingFiveAndOffersAnEnchantmentFromThoseCards() {
        SidequestCatchAFish enchantment = new SidequestCatchAFish();
        harness.setLibrary(player1, List.of(
                enchantment, new FireMagic(), new DwarvenCastleGuard(), new Forest(), new FireMagic()));
        harness.castFromHand(player1, new TerraMagicalAdept(), "{1}{R}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(enchantment.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void tranceTransformsTerraIntoEsperTerraWithItsFirstLoreCounter() {
        Permanent terra = addReadyTerra();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(terra), null, null);
        harness.passBothPriorities();

        Permanent transformed = findPermanent(player1, EsperTerra.class);
        assertThat(transformed.isTransformed()).isTrue();
        assertThat(transformed.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    void chaptersCopyAnEnchantmentWithHasteAndSacrificeItAtTheNextEndStep() {
        Permanent terra = addEsperTerraWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SidequestCatchAFish());

        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(terra.getCounterCount(CounterType.LORE)).isEqualTo(1);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    @Test
    void copiedSagaCanChooseUpToThreeAdditionalLoreCounters() {
        addEsperTerraWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SummonGFIfrit());

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCounterCount(CounterType.LORE)).isEqualTo(1);
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.context()).isEqualTo(new ChoiceContext.NumberChoice(token.getId()));
        assertThat(choice.options()).containsExactly("0", "1", "2", "3");

        harness.handleListChoice(player1, "0");
        assertThat(token.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    void fourthChapterAddsTwoOfEachColorAndReturnsToTheFrontFace() {
        addEsperTerraWithLore(3);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        Permanent returned = findPermanent(player1, TerraMagicalAdept.class);
        assertThat(returned.isTransformed()).isFalse();
    }

    @Test
    void canDeclineAnEnchantmentFromAShortLibrary() {
        SidequestCatchAFish enchantment = new SidequestCatchAFish();
        harness.setLibrary(player1, List.of(enchantment, new Forest()));
        harness.castFromHand(player1, new TerraMagicalAdept(), "{1}{R}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enchantment);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(enchantment);
    }

    @Test
    void cannotReturnAnEnchantmentAlreadyInTheGraveyard() {
        SidequestCatchAFish oldEnchantment = new SidequestCatchAFish();
        harness.setGraveyard(player1, List.of(oldEnchantment));
        harness.setLibrary(player1, List.of(new Forest(), new FireMagic()));
        harness.castFromHand(player1, new TerraMagicalAdept(), "{1}{R}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oldEnchantment);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(oldEnchantment);
    }

    @Test
    void canReturnOnlyOneOfMultipleMilledEnchantments() {
        SidequestCatchAFish first = new SidequestCatchAFish();
        SummonGFIfrit second = new SummonGFIfrit();
        harness.setLibrary(player1, List.of(first, second, new Forest()));
        harness.castFromHand(player1, new TerraMagicalAdept(), "{1}{R}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card == first || card == second)).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void tranceCannotBeActivatedOutsideAMainPhase() {
        Permanent terra = addReadyTerra();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(terra), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secondAndThirdChaptersAlsoCopyEnchantments() {
        for (int startingLore = 1; startingLore <= 2; startingLore++) {
            gd.playerBattlefields.get(player1.getId()).clear();
            gd.stack.clear();
            addEsperTerraWithLore(startingLore);
            Permanent target = harness.addToBattlefieldAndReturn(player1, new SidequestCatchAFish());
            advanceToNextChapter();
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        }
    }

    @Test
    void sagaCountersAreChosenDuringTheCopyChapterResolution() {
        addEsperTerraWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SummonGFIfrit());
        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "3");
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.getCounterCount(CounterType.LORE)).isEqualTo(4);
    }

    @Test
    void chapterCannotCopyAnOpponentsEnchantmentOrLegendaryEnchantment() {
        Permanent terra = addEsperTerraWithLore(0);
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new SidequestCatchAFish());
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player1, new SidequestCatchAFish());
        advanceToNextChapter();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentEnchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, terra.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, legalTarget.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
    }

    @Test
    void returningFrontFaceTriggersMillingAgain() {
        addEsperTerraWithLore(3);
        SidequestCatchAFish enchantment = new SidequestCatchAFish();
        harness.setLibrary(player1, List.of(enchantment, new Forest(), new FireMagic(),
                new DwarvenCastleGuard(), new Forest()));
        advanceToNextChapter();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    void tranceReturnsStolenTerraUnderItsOwnersControl() {
        TerraMagicalAdept card = new TerraMagicalAdept();
        card.setOwnerId(player2.getId());
        Permanent terra = harness.addToBattlefieldAndReturn(player1, card);
        terra.setSummoningSick(false);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(terra), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(terra);
        Permanent returned = findPermanent(player2, EsperTerra.class);
        assertThat(returned.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    void copiedEnchantmentSurvivesOpponentsEndStepUntilYourNextEndStep() {
        addEsperTerraWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SidequestCatchAFish());
        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    private Permanent addReadyTerra() {
        Permanent terra = harness.addToBattlefieldAndReturn(player1, new TerraMagicalAdept());
        terra.setSummoningSick(false);
        return terra;
    }

    private Permanent addEsperTerraWithLore(int loreCounters) {
        TerraMagicalAdept front = new TerraMagicalAdept();
        Permanent terra = new Permanent(front);
        terra.setCard(front.getBackFaceCard());
        terra.setTransformed(true);
        terra.setSummoningSick(false);
        terra.setCounterCount(CounterType.LORE, loreCounters);
        gd.playerBattlefields.get(player1.getId()).add(terra);
        return terra;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent findPermanent(com.github.laxika.magicalvibes.model.Player player, Class<?> cardClass) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> cardClass.isInstance(permanent.getCard()))
                .findFirst()
                .orElseThrow();
    }
}
