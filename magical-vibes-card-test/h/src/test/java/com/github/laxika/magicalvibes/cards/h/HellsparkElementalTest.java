package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HellsparkElemental.class})
@DisplayName("Hellspark Elemental")
class HellsparkElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself at the end step")
    void sacrificesItselfAtEndStep() {
        harness.addToBattlefield(player1, new HellsparkElemental());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Hellspark Elemental");
        harness.assertInGraveyard(player1, "Hellspark Elemental");
    }

    @Test
    @DisplayName("Unearth returns Hellspark Elemental to the battlefield with haste")
    void unearthReturnsWithHaste() {
        HellsparkElemental card = new HellsparkElemental();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Hellspark Elemental");
        assertThat(perm.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Hellspark Elemental");
    }

    @Test
    @DisplayName("Unearthed Hellspark Elemental is exiled at the next end step")
    void unearthExiledAtEndStep() {
        HellsparkElemental card = new HellsparkElemental();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Hellspark Elemental");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hellspark Elemental"));
    }

    @Test
    void sacrificesItselfAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new HellsparkElemental());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Hellspark Elemental");
        harness.assertInGraveyard(player1, "Hellspark Elemental");
    }

    @Test
    void unearthExilesInsteadOfDyingBeforeEndStep() {
        harness.setGraveyard(player1, List.of(new HellsparkElemental()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        findPermanent(player1, "Hellspark Elemental").setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Hellspark Elemental");
        harness.assertNotInGraveyard(player1, "Hellspark Elemental");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hellspark Elemental"));
    }

    @Test
    void cannotUnearthOutsideMainPhase() {
        harness.setGraveyard(player1, List.of(new HellsparkElemental()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Hellspark Elemental");
        harness.assertNotOnBattlefield(player1, "Hellspark Elemental");
    }

    @Test
    void cannotUnearthDuringOpponentsTurn() {
        harness.setGraveyard(player1, List.of(new HellsparkElemental()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Hellspark Elemental");
    }

    @Test
    void cannotUnearthWithAnotherAbilityOnStack() {
        harness.setGraveyard(player1, List.of(new HellsparkElemental(), new HellsparkElemental()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Hellspark Elemental")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotUnearthWithoutPayingGenericMana() {
        harness.setGraveyard(player1, List.of(new HellsparkElemental()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Hellspark Elemental");
        harness.assertNotOnBattlefield(player1, "Hellspark Elemental");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unearthedCreatureCanAttackImmediately() {
        harness.setGraveyard(player1, List.of(new HellsparkElemental()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }
}
