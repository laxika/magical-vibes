package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoblinHero;
import com.github.laxika.magicalvibes.cards.p.Preacher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SafeHaven.class, GoblinHero.class, Preacher.class})
class SafeHavenTest extends BaseCardTest {

    @Test
    @DisplayName("{2}, {T}: Exile target creature you control, tracked with Safe Haven")
    void exileAbilityExilesOwnCreature() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new SafeHaven());
        harness.addToBattlefield(player1, new GoblinHero());
        Permanent goblin = findPermanent(player1, "Goblin Hero");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Hero");
        assertThat(gd.getCardsExiledByPermanent(haven.getId()))
                .anyMatch(card -> card.getName().equals("Goblin Hero"));
    }

    @Test
    @DisplayName("{2}, {T} cannot target a creature you don't control")
    void exileAbilityCannotTargetOpponentCreature() {
        harness.addToBattlefieldAndReturn(player1, new SafeHaven());
        harness.addToBattlefield(player2, new GoblinHero());
        Permanent goblin = findPermanent(player2, "Goblin Hero");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Accepting the upkeep trigger sacrifices Safe Haven and returns its exiled creature")
    void acceptingUpkeepTriggerSacrificesAndReturnsCreature() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new SafeHaven());
        harness.addToBattlefield(player1, new GoblinHero());
        Permanent goblin = findPermanent(player1, "Goblin Hero");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Safe Haven");
        harness.assertOnBattlefield(player1, "Goblin Hero");
        assertThat(gd.getCardsExiledByPermanent(haven.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves Safe Haven and its exiled creature in place")
    void decliningUpkeepTriggerDoesNothing() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new SafeHaven());
        harness.addToBattlefield(player1, new GoblinHero());
        Permanent goblin = findPermanent(player1, "Goblin Hero");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Safe Haven");
        harness.assertNotOnBattlefield(player1, "Goblin Hero");
        assertThat(gd.getCardsExiledByPermanent(haven.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Accepting the upkeep trigger returns every exiled creature to its owner's battlefield")
    void acceptingUpkeepTriggerReturnsEveryExiledCreatureToItsOwner() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new SafeHaven());
        addCreatureReady(player1, new Preacher());
        Permanent ownedGoblin = addCreatureReady(player1, new GoblinHero());
        Permanent borrowedGoblin = addCreatureReady(player2, new GoblinHero());
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.handlePermanentChosen(player2, borrowedGoblin.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, ownedGoblin.getId());
        harness.passBothPriorities();
        haven.untap();
        harness.activateAbility(player1, 0, null, borrowedGoblin.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(haven.getId())).hasSize(2);

        gd.turnNumber = 2;
        advanceToUpkeepWithPreacherStayingTapped();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Safe Haven");
        assertThat(findPermanents(player1, "Goblin Hero"))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(ownedGoblin.getCard().getId());
        assertThat(findPermanents(player2, "Goblin Hero"))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(borrowedGoblin.getCard().getId());
        assertThat(gd.getCardsExiledByPermanent(haven.getId())).isEmpty();
    }

    private void advanceToUpkeepWithPreacherStayingTapped() {
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UNTAP);
        harness.handleMayAbilityChosen(player1, false);
        harness.passUntil(player1, TurnStep.UPKEEP);
    }
}
