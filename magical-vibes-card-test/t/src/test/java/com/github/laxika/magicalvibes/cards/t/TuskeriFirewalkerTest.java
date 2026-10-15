package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FearlessPup;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredMountain;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({TuskeriFirewalker.class, FearlessPup.class, SnowCoveredMountain.class})
class TuskeriFirewalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Boast exiles the top card and lets its controller play it this turn")
    void boastExilesTopCardWithPlayPermission() {
        Permanent firewalker = addCreatureReady(player1, new TuskeriFirewalker());
        Card top = new FearlessPup();
        harness.setLibrary(player1, List.of(top));
        firewalker.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
    }

    @Test
    void exiledCreatureRequiresItsNormalManaCost() {
        Permanent firewalker = addCreatureReady(player1, new TuskeriFirewalker());
        Card top = new FearlessPup();
        harness.setLibrary(player1, List.of(top));
        firewalker.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, top.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fearless Pup");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void playPermissionDoesNotBypassCreatureSpellTiming() {
        Permanent firewalker = addCreatureReady(player1, new TuskeriFirewalker());
        Card top = new FearlessPup();
        harness.setLibrary(player1, List.of(top));
        firewalker.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromExile(player1, top.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Fearless Pup");
    }

    @Test
    void exiledLandCanBePlayedButDoesNotGrantAnotherLandPlay() {
        Permanent firewalker = addCreatureReady(player1, new TuskeriFirewalker());
        Card top = new SnowCoveredMountain();
        harness.setLibrary(player1, List.of(top));
        firewalker.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castFromExile(player1, top.getId());

        harness.assertOnBattlefield(player1, "Snow-Covered Mountain");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        harness.setHand(player1, List.of(new SnowCoveredMountain()));
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unplayedCardStaysExiledAfterPermissionExpires() {
        Permanent firewalker = addCreatureReady(player1, new TuskeriFirewalker());
        Card top = new FearlessPup();
        harness.setLibrary(player1, List.of(top, new FearlessPup()));
        firewalker.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(top.getId());
    }

    @Test
    void boastingWithEmptyLibraryDoesNotDrawOrLoseTheGame() {
        Permanent firewalker = addCreatureReady(player1, new TuskeriFirewalker());
        harness.setLibrary(player1, List.of());
        firewalker.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Boast requires Tuskeri Firewalker to have attacked this turn")
    void boastRequiresThisCreatureToHaveAttacked() {
        addCreatureReady(player1, new TuskeriFirewalker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    @DisplayName("Boast can be activated only once each turn")
    void boastOnlyOncePerTurn() {
        Permanent firewalker = addCreatureReady(player1, new TuskeriFirewalker());
        firewalker.setAttackedThisTurn(true);
        harness.setLibrary(player1, List.of(new FearlessPup(), new FearlessPup()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }
}
