package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.r.ResoundingThunder;
import com.github.laxika.magicalvibes.model.Keyword;
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

@DisplayName("Sedris, the Traitor King")
@CardUsed({SedrisTheTraitorKing.class, CylianElf.class, ResoundingThunder.class, Snakeform.class})
class SedrisTheTraitorKingTest extends BaseCardTest {

    @Test
    @DisplayName("Grants unearth {2}{B} to a creature card in your graveyard")
    void grantsUnearthToOwnedCreatureCard() {
        addSedris(player1);
        harness.setGraveyard(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateGraveyardAbility(player1, 0, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Cylian Elf");
        assertThat(harness.getGameQueryService().hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Cylian Elf");
    }

    @Test
    @DisplayName("Unearthed creature is exiled at the next end step")
    void unearthedCreatureExiledAtEndStep() {
        addSedris(player1);
        harness.setGraveyard(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateGraveyardAbility(player1, 0, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Cylian Elf");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cylian Elf"));
    }

    @Test
    @DisplayName("Without Sedris, a creature card in the graveyard has no unearth ability")
    void noUnearthWithoutSedris() {
        harness.setGraveyard(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not grant unearth to a noncreature card in your graveyard")
    void doesNotGrantUnearthToNoncreatureCard() {
        addSedris(player1);
        harness.setGraveyard(player1, List.of(new ResoundingThunder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not grant unearth to an opponent's creature cards")
    void doesNotGrantUnearthToOpponent() {
        addSedris(player1);
        harness.setGraveyard(player2, List.of(new CylianElf()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unearth cannot be activated outside a main phase")
    void unearthRequiresMainPhase() {
        addSedris(player1);
        harness.setGraveyard(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unearth cannot be activated with another ability on the stack")
    void unearthRequiresEmptyStack() {
        addSedris(player1);
        harness.setGraveyard(player1, List.of(new CylianElf(), new CylianElf()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.activateGraveyardAbility(player1, 0, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unearth requires black mana in addition to two generic mana")
    void unearthRequiresBlackMana() {
        addSedris(player1);
        harness.setGraveyard(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Cylian Elf");
    }

    @Test
    @DisplayName("Unearth returns only the card whose ability was activated")
    void returnsOnlyActivatingCard() {
        addSedris(player1);
        CylianElf first = new CylianElf();
        CylianElf second = new CylianElf();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateGraveyardAbility(player1, 1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(findPermanent(player1, "Cylian Elf").getCard().getId()).isEqualTo(second.getId());
    }

    @Test
    @DisplayName("A lethally damaged unearthed creature is exiled instead of dying")
    void unearthedCreatureExiledInsteadOfDying() {
        addSedris(player1);
        harness.setGraveyard(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateGraveyardAbility(player1, 0, 0);
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Cylian Elf");
        harness.setHand(player1, List.of(new ResoundingThunder()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, returned.getId());

        harness.assertNotOnBattlefield(player1, "Cylian Elf");
        harness.assertNotInGraveyard(player1, "Cylian Elf");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(returned.getCard().getId()));
    }

    @Test
    @DisplayName("Sedris stops granting unearth when it loses all abilities")
    void noUnearthWhenSedrisLosesAbilities() {
        Permanent sedris = addSedris(player1);
        harness.setGraveyard(player1, List.of(new CylianElf()));
        harness.setHand(player1, List.of(new Snakeform()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, sedris.getId());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An activated unearth ability still resolves after Sedris loses its abilities")
    void activatedUnearthSurvivesAbilityRemoval() {
        Permanent sedris = addSedris(player1);
        harness.setGraveyard(player1, List.of(new CylianElf()));
        harness.setHand(player1, List.of(new Snakeform()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateGraveyardAbility(player1, 0, 0);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, sedris.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cylian Elf");
        harness.assertNotInGraveyard(player1, "Cylian Elf");
    }

    @Test
    @DisplayName("Losing all abilities does not remove an unearthed creature's delayed exile")
    void abilityRemovalDoesNotPreventDelayedExile() {
        addSedris(player1);
        harness.setGraveyard(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateGraveyardAbility(player1, 0, 0);
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Cylian Elf");
        harness.setHand(player1, List.of(new Snakeform()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, returned.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cylian Elf");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(returned.getCard().getId()));
    }

    private Permanent addSedris(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SedrisTheTraitorKing());
    }
}
