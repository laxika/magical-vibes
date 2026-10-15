package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.z.ZanikevLocust;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BonecacheOverseer.class, BakersbaneDuo.class, ZanikevLocust.class})
class BonecacheOverseerTest extends BaseCardTest {

    @Test
    @DisplayName("Requires three cards to have left the graveyard")
    void requiresThreeCardsToLeaveGraveyard() {
        addCreatureReady(player1, new BonecacheOverseer());
        Permanent target = addCreatureReady(player2, new BakersbaneDuo());
        harness.setLibrary(player1, List.of(new BakersbaneDuo()));
        harness.setGraveyard(player1, List.of(new ZanikevLocust(), new ZanikevLocust(), new ZanikevLocust()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        leaveGraveyardCard(target);
        leaveGraveyardCard(target);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if");

        leaveGraveyardCard(target);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInHand(player1, "Bakersbane Duo");
    }

    @Test
    @DisplayName("A sacrificed Food enables the draw ability")
    void sacrificedFoodEnablesDraw() {
        addCreatureReady(player1, new BonecacheOverseer());
        addFoodToken(player1);
        harness.setLibrary(player1, List.of(new BakersbaneDuo()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Food")), null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertInHand(player1, "Bakersbane Duo");
    }

    @Test
    void cannotActivateWithoutEitherQualifyingEvent() {
        Permanent overseer = addCreatureReady(player1, new BonecacheOverseer());
        harness.setLibrary(player1, List.of(new BakersbaneDuo()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if");

        harness.assertLife(player1, 20);
        assertThat(overseer.isTapped()).isFalse();
        harness.assertNotInHand(player1, "Bakersbane Duo");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void foodSacrificeEnablesActivationBeforeFoodAbilityResolves() {
        Permanent overseer = addCreatureReady(player1, new BonecacheOverseer());
        addFoodToken(player1);
        harness.setLibrary(player1, List.of(new BakersbaneDuo()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Food")), null, null);
        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 19);
        assertThat(overseer.isTapped()).isTrue();
        harness.assertNotInHand(player1, "Bakersbane Duo");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Bakersbane Duo");
        harness.assertLife(player1, 19);
        resolveAllTriggers();
        harness.assertLife(player1, 22);
    }

    @Test
    void opponentsFoodSacrificeDoesNotEnableActivation() {
        addCreatureReady(player1, new BonecacheOverseer());
        addFoodToken(player2);
        harness.setLibrary(player1, List.of(new BakersbaneDuo()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(findPermanent(player2, "Food")), null, null);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if");
        harness.assertLife(player1, 20);
    }

    @Test
    void foodSacrificedBeforeOverseerEntersEnablesEachOverseer() {
        addFoodToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Food")), null, null);
        resolveAllTriggers();
        addCreatureReady(player1, new BonecacheOverseer());
        addCreatureReady(player1, new BonecacheOverseer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new BakersbaneDuo(), new BakersbaneDuo()));

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void foodSacrificeFromPreviousTurnDoesNotEnableActivation() {
        addCreatureReady(player1, new BonecacheOverseer());
        addFoodToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Food")), null, null);
        resolveAllTriggers();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if");
    }

    @Test
    void opponentsGraveyardDeparturesDoNotEnableActivation() {
        addCreatureReady(player1, new BonecacheOverseer());
        Permanent target = addCreatureReady(player2, new BakersbaneDuo());
        harness.setGraveyard(player2, List.of(new ZanikevLocust(), new ZanikevLocust(), new ZanikevLocust()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLACK, 6);
        harness.addMana(player2, ManaColor.COLORLESS, 6);

        for (int i = 0; i < 3; i++) {
            harness.activateGraveyardAbility(player2, 0, target.getId());
            harness.passBothPriorities();
        }

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if");
    }

    @Test
    void graveyardDeparturesFromPreviousTurnDoNotEnableActivation() {
        addCreatureReady(player1, new BonecacheOverseer());
        Permanent target = addCreatureReady(player2, new BakersbaneDuo());
        harness.setGraveyard(player1, List.of(new ZanikevLocust(), new ZanikevLocust(), new ZanikevLocust()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        for (int i = 0; i < 3; i++) {
            leaveGraveyardCard(target);
        }
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if");
    }

    @Test
    void qualifyingEventDoesNotBypassSummoningSickness() {
        harness.addToBattlefield(player1, new BonecacheOverseer());
        addFoodToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Food")), null, null);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 23);
        assertThat(findPermanent(player1, "Bonecache Overseer").isTapped()).isFalse();
    }

    @Test
    void drawAbilityCannotBeActivatedAgainWhileTapped() {
        addCreatureReady(player1, new BonecacheOverseer());
        addFoodToken(player1);
        harness.setLibrary(player1, List.of(new BakersbaneDuo(), new BakersbaneDuo()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Food")), null, null);
        resolveAllTriggers();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 22);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void leaveGraveyardCard(Permanent target) {
        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addFoodToken(Player player) {
        harness.enterBattlefieldAndReturn(player, new BakersbaneDuo());
        resolveAllTriggers();
    }
}
