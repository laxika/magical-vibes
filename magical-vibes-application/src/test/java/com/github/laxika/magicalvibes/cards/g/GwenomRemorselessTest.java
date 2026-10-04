package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThaliaGuardianOfThraben;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GwenomRemorseless.class, Forest.class, Shock.class, ThaliaGuardianOfThraben.class})
class GwenomRemorselessTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking grants top-library spell casts for life")
    void attackingGrantsTopLibrarySpellCastsForLife() {
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        addCreatureReady(player1, new GwenomRemorseless());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        int lifeAfterAttack = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveFromLibraryTop(player1, player2.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeAfterAttack - shock.getManaValue());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(shock);
    }

    @Test
    @DisplayName("Attacking permits a land from the top with normal land timing")
    void attackingPermitsTopLibraryLandPlay() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        addCreatureReady(player1, new GwenomRemorseless());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The top-library permission expires at cleanup")
    void permissionExpiresAtCleanup() {
        harness.setLibrary(player1, List.of(new Shock()));
        addCreatureReady(player1, new GwenomRemorseless());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.libraryTopCardLifePlayPermissionsUntilEndOfTurn).contains(player1.getId());
        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gd.libraryTopCardLifePlayPermissionsUntilEndOfTurn).doesNotContain(player1.getId());
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotCastFromLibraryBeforeAttacking() {
        harness.setLibrary(player1, List.of(new Shock()));
        addCreatureReady(player1, new GwenomRemorseless());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void permissionAllowsSuccessiveTopCardsWithoutManaAndSurvivesSourceLeaving() {
        Shock first = new Shock();
        Shock second = new Shock();
        harness.setLibrary(player1, List.of(first, second));
        addCreatureReady(player1, new GwenomRemorseless());
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();
        int lifeBeforeCasting = gd.getLife(player1.getId());
        int opponentLifeBeforeCasting = gd.getLife(player2.getId());

        harness.castAndResolveFromLibraryTop(player1, player2.getId());
        harness.castAndResolveFromLibraryTop(player1, player2.getId());

        harness.assertLife(player1, lifeBeforeCasting - 2);
        harness.assertLife(player2, opponentLifeBeforeCasting - 4);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
    }

    @Test
    void permissionDoesNotAllowLandDuringCombatOrAnExtraLandPlay() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        addCreatureReady(player1, new GwenomRemorseless());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
        });

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        int lifeBeforePlaying = gd.getLife(player1.getId());
        harness.castFromLibraryTop(player1);

        harness.assertLife(player1, lifeBeforePlaying);
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    void creatureStillRequiresSorceryTimingAndEnoughLife() {
        GwenomRemorseless topCard = new GwenomRemorseless();
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new GwenomRemorseless());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
        });

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.setLife(player1, 4);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 4);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setLife(player1, 20);
        harness.castAndResolveFromLibraryTop(player1);

        harness.assertLife(player1, 15);
        harness.assertOnBattlefield(player1, "Gwenom, Remorseless");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void lifeAlternativeCannotBypassAnUnpaidSpellTax() {
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        addCreatureReady(player1, new GwenomRemorseless());
        harness.addToBattlefield(player2, new ThaliaGuardianOfThraben());
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        int lifeBeforeCasting = gd.getLife(player1.getId());

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, lifeBeforeCasting);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
    }

    @Test
    void lifeAlternativePaysSpellTaxInMana() {
        harness.setLibrary(player1, List.of(new Shock()));
        addCreatureReady(player1, new GwenomRemorseless());
        harness.addToBattlefield(player2, new ThaliaGuardianOfThraben());
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBeforeCasting = gd.getLife(player1.getId());

        harness.castAndResolveFromLibraryTop(player1, player2.getId());

        harness.assertLife(player1, lifeBeforeCasting - 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }
}
