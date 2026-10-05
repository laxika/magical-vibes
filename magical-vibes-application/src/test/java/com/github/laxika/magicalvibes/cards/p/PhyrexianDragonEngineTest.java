package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MachineOverMatter;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.NoOneLeftBehind;
import com.github.laxika.magicalvibes.cards.r.Reanimate;
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

@CardUsed({PhyrexianDragonEngine.class, Forest.class, Mountain.class,
        MachineOverMatter.class, NoOneLeftBehind.class, Reanimate.class})
class PhyrexianDragonEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Casting it from hand does not trigger its graveyard-entered ability")
    void castingFromHandDoesNotTriggerGraveyardAbility() {
        harness.castFromHand(player1, new PhyrexianDragonEngine(), "{3}");
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Phyrexian Dragon Engine");
    }

    @Test
    @DisplayName("Unearth lets you discard your hand and draw three cards")
    void unearthMayDiscardHandAndDrawThree() {
        prepareUnearth();

        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Forest(), new Mountain()));

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Declining the graveyard-entered ability keeps your hand")
    void decliningGraveyardAbilityKeepsHand() {
        prepareUnearth();
        harness.setHand(player1, List.of(new Forest(), new Mountain()));

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Discarding an empty hand still draws three cards")
    void unearthCanDiscardEmptyHandAndDrawThree() {
        prepareUnearth();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void returningFromOwnGraveyardWithoutUnearthStillOffersDiscardAndDraw() {
        PhyrexianDragonEngine engine = new PhyrexianDragonEngine();
        harness.setGraveyard(player1, List.of(engine));
        harness.setHand(player1, List.of(new NoOneLeftBehind(), new Mountain()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, engine.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Phyrexian Dragon Engine");
    }

    @Test
    @CardUsed({PhyrexianDragonEngine.class, Reanimate.class, Mountain.class})
    void returningFromOpponentsGraveyardDoesNotOfferDiscardAndDraw() {
        PhyrexianDragonEngine engine = new PhyrexianDragonEngine();
        harness.setGraveyard(player2, List.of(engine));
        harness.setHand(player1, List.of(new Reanimate(), new Mountain()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, engine.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Phyrexian Dragon Engine");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Mountain");
    }

    @Test
    void unearthedEngineHasHasteAndIsExiledAtNextEndStep() {
        prepareUnearth();
        harness.setHand(player1, List.of());

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        Permanent engine = findPermanent(player1, "Phyrexian Dragon Engine");
        assertThat(gqs.hasKeyword(gd, engine, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Phyrexian Dragon Engine");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(engine.getOriginalCard().getId()));
    }

    @Test
    void returningUnearthedEngineToHandExilesItInstead() {
        prepareUnearth();
        harness.setHand(player1, List.of());
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        Permanent engine = findPermanent(player1, "Phyrexian Dragon Engine");
        harness.setHand(player1, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, engine.getId());

        harness.assertNotOnBattlefield(player1, "Phyrexian Dragon Engine");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(engine.getOriginalCard().getId()));
    }

    @Test
    void unearthCannotBeActivatedDuringCombat() {
        prepareUnearth();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Phyrexian Dragon Engine");
    }

    private void prepareUnearth() {
        harness.setGraveyard(player1, List.of(new PhyrexianDragonEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
    }

}
