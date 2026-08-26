package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GwenomRemorseless.class, Forest.class, Shock.class})
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
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

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
}
