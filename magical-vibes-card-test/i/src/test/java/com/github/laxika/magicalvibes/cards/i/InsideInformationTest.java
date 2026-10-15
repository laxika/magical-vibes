package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThaliaGuardianOfThraben;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InsideInformation.class, Shock.class, Forest.class, ThaliaGuardianOfThraben.class})
class InsideInformationTest extends BaseCardTest {

    @Test
    void exilesXCardsFromTargetOpponentsLibraryAndGrantsPlayPermission() {
        Card first = new Shock();
        Card second = new Forest();
        harness.setLibrary(player2, List.of(first, second));
        harness.setHand(player1, List.of(new InsideInformation()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayForLifeEqualToManaValue).contains(first.getId(), second.getId());
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void exiledSpellCanBeCastForLifeAndExiledLandUsesNormalLandPlay() {
        Shock shock = new Shock();
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(shock, forest));
        castInsideInformation(2);

        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, forest.getId());

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertLife(player1, 19);
    }

    @Test
    void targetMustBeAnOpponent() {
        harness.setHand(player1, List.of(new InsideInformation()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void playPermissionExpiresAtCleanup() {
        Shock shock = new Shock();
        harness.setLibrary(player2, List.of(shock));
        castInsideInformation(1);

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(shock.getId());
        assertThat(gd.exilePlayForLifeEqualToManaValue).doesNotContain(shock.getId());
    }

    @Test
    void zeroXLeavesLibraryUntouched() {
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(forest));

        castInsideInformation(0);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void exilesOnlyAvailableCardsWhenXExceedsLibrarySize() {
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(forest));

        castInsideInformation(3);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotChooseNonzeroXWhenCastingExiledSpellForLife() {
        InsideInformation exiledSpell = new InsideInformation();
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(exiledSpell, forest));
        castInsideInformation(1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1,
                exiledSpell.getId(), 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(exiledSpell);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest);
        harness.assertLife(player1, 20);
    }

    @Test
    void castingForLifeStillRequiresManaForCostIncreases() {
        InsideInformation exiledSpell = new InsideInformation();
        harness.setLibrary(player2, List.of(exiledSpell));
        castInsideInformation(1);
        harness.addToBattlefield(player2, new ThaliaGuardianOfThraben());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1,
                exiledSpell.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(exiledSpell);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, exiledSpell.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player2, "Inside Information");
    }

    private void castInsideInformation(int xValue) {
        harness.setHand(player1, List.of(new InsideInformation()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castSorcery(player1, 0, xValue, player2.getId());
        harness.passBothPriorities();
    }
}
