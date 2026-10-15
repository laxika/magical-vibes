package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AbundantHarvest;
import com.github.laxika.magicalvibes.cards.d.DeepwoodDenizen;
import com.github.laxika.magicalvibes.cards.d.DreyKeeper;
import com.github.laxika.magicalvibes.cards.k.KalonianBehemoth;
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

@CardUsed({ChainerNightmareAdept.class, AbundantHarvest.class, DeepwoodDenizen.class,
        DreyKeeper.class, KalonianBehemoth.class})
class ChainerNightmareAdeptTest extends BaseCardTest {

    @Test
    void discardingAllowsOneCreatureCastFromGraveyard() {
        harness.addToBattlefield(player1, new ChainerNightmareAdept());
        Card bear = new DeepwoodDenizen();
        harness.setGraveyard(player1, List.of(bear));
        harness.setHand(player1, List.of(new DeepwoodDenizen()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();

        Permanent bearPermanent = findPermanent(player1, "Deepwood Denizen");
        assertThat(gqs.hasKeyword(gd, bearPermanent, Keyword.HASTE)).isTrue();

        harness.setGraveyard(player1, List.of(new DeepwoodDenizen()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nonHandCreatureEntersWithHasteUntilYourNextTurn() {
        harness.addToBattlefield(player1, new ChainerNightmareAdept());

        Permanent bear = harness.enterBattlefieldAndReturn(player1, new DeepwoodDenizen());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isTrue();
    }

    @Test
    void creatureCastFromHandDoesNotGainHaste() {
        harness.addToBattlefield(player1, new ChainerNightmareAdept());
        harness.castFromHand(player1, new DeepwoodDenizen(), "{2}{G}");
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Deepwood Denizen");
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isFalse();
    }

    @Test
    void chainerEnteringWithoutBeingCastGrantsItselfHaste() {
        Permanent chainer = harness.enterBattlefieldAndReturn(player1, new ChainerNightmareAdept());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, chainer, Keyword.HASTE)).isTrue();
    }

    @Test
    void chainerCastFromGraveyardGrantsItselfHaste() {
        harness.addToBattlefield(player1, new ChainerNightmareAdept());
        harness.setHand(player1, List.of(new ChainerNightmareAdept()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Chainer, Nightmare Adept"), Keyword.HASTE))
                .isTrue();
    }

    @Test
    void discardedCreatureCanBeCastUsingThePermission() {
        harness.addToBattlefield(player1, new ChainerNightmareAdept());
        harness.setHand(player1, List.of(new DeepwoodDenizen()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Deepwood Denizen");
        harness.assertNotInGraveyard(player1, "Deepwood Denizen");
    }

    @Test
    void cannotActivateTwiceInOneTurn() {
        harness.addToBattlefield(player1, new ChainerNightmareAdept());
        harness.setHand(player1, List.of(new DeepwoodDenizen(), new DeepwoodDenizen()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsCreatureDoesNotGainHaste() {
        harness.addToBattlefield(player1, new ChainerNightmareAdept());
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new DeepwoodDenizen());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    void hasteExpiresAtTheBeginningOfControllersNextTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new ChainerNightmareAdept());
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new DeepwoodDenizen());
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    void graveyardPermissionExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new ChainerNightmareAdept());
        harness.setHand(player1, List.of(new DeepwoodDenizen()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({ChainerNightmareAdept.class, KalonianBehemoth.class})
    void enteringCreatureWithShroudStillGainsHaste() {
        harness.addToBattlefield(player1, new ChainerNightmareAdept());
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new KalonianBehemoth());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @CardUsed({ChainerNightmareAdept.class, DreyKeeper.class})
    void creatureTokensDoNotGainHaste() {
        harness.addToBattlefield(player1, new ChainerNightmareAdept());
        harness.castFromHand(player1, new DreyKeeper(), "{3}{B}{G}");
        resolveAllTriggers();

        List<Permanent> squirrels = findPermanents(player1, "Squirrel");
        assertThat(squirrels).hasSize(2);
        assertThat(squirrels).allSatisfy(squirrel ->
                assertThat(gqs.hasKeyword(gd, squirrel, Keyword.HASTE)).isFalse());
    }

    @Test
    void permissionDoesNotAllowNoncreatureSpells() {
        harness.addToBattlefield(player1, new ChainerNightmareAdept());
        harness.setHand(player1, List.of(new AbundantHarvest()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityCanBeActivatedOnOpponentsTurnButDoesNotChangeCreatureTiming() {
        harness.addToBattlefield(player1, new ChainerNightmareAdept());
        harness.setHand(player1, List.of(new DeepwoodDenizen()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        harness.ensurePriority(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
