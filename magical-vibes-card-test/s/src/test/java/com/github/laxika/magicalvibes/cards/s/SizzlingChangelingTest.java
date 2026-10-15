package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SizzlingChangeling.class, Pyroclasm.class, Shock.class, Mountain.class})
class SizzlingChangelingTest extends BaseCardTest {

    private Card resolveDeathTrigger() {
        Card topCard = new Shock();
        resolveDeathTrigger(List.of(topCard));
        return topCard;
    }

    private void resolveDeathTrigger(List<? extends Card> library) {
        harness.addToBattlefield(player1, new SizzlingChangeling());
        harness.setLibrary(player1, library);

        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("When Sizzling Changeling dies, the top card can be played from exile for its normal cost")
    void deathTriggerExilesTopCardAndGrantsPlayPermission() {
        Card shock = resolveDeathTrigger();

        harness.assertInGraveyard(player1, "Sizzling Changeling");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
        assertThat(gd.exilePlayPermissions.get(shock.getId())).isEqualTo(player1.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void onlyTheTopCardIsExiled() {
        Card top = new Shock();
        Card second = new Mountain();
        resolveDeathTrigger(List.of(top, second));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    void emptyLibraryDoesNotExileAnything() {
        resolveDeathTrigger(List.of());

        harness.assertInGraveyard(player1, "Sizzling Changeling");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exiledSpellStillRequiresMana() {
        Card shock = resolveDeathTrigger();

        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    @Test
    void opponentCannotUsePlayPermission() {
        Card shock = resolveDeathTrigger();
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.castFromExile(player2, shock.getId(), player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    @Test
    void exiledSorceryStillRequiresSorceryTiming() {
        Card sorcery = new Pyroclasm();
        resolveDeathTrigger(List.of(sorcery));
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, sorcery.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(sorcery);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, sorcery.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Pyroclasm");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(sorcery);
    }

    @Test
    void exiledLandCanBePlayedButDoesNotGrantAnExtraLandPlay() {
        Card land = new Mountain();
        resolveDeathTrigger(List.of(land));
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(land);
        harness.setHand(player1, List.of(new Mountain()));
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void playPermissionLastsThroughNextTurnAndThenExpires() {
        Card shock = resolveDeathTrigger();
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain(), new Mountain()));

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions.get(shock.getId())).isEqualTo(player1.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions.get(shock.getId())).isEqualTo(player1.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(shock.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.ensurePriority(player1);
        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
