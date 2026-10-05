package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CloudfinRaptor;
import com.github.laxika.magicalvibes.cards.d.DimirGuildgate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
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

@CardUsed({NightveilSpecter.class, CloudfinRaptor.class, DimirGuildgate.class})
class NightveilSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player exiles the top card of that player's library, tracked with the Specter")
    void combatDamageExilesTopCardOfDamagedPlayerLibrary() {
        Permanent specter = addAttackingSpecter(player1);
        harness.setLibrary(player2, List.of(new CloudfinRaptor(), new CloudfinRaptor(), new CloudfinRaptor()));

        resolveCombatAndTrigger();

        assertThat(gd.getCardsExiledByPermanent(specter.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The exiled card is exiled face up")
    void exiledCardIsFaceUp() {
        Permanent specter = addAttackingSpecter(player1);
        harness.setLibrary(player2, List.of(new CloudfinRaptor(), new CloudfinRaptor(), new CloudfinRaptor()));

        resolveCombatAndTrigger();

        assertThat(gd.exiledCards).filteredOn(e -> specter.getId().equals(e.sourcePermanentId()))
                .isNotEmpty()
                .noneMatch(ExiledCardEntry::faceDown);
    }

    @Test
    @DisplayName("The Specter's controller may cast a card exiled with it, paying its normal cost")
    void controllerMayCastExiledCard() {
        Permanent specter = addAttackingSpecter(player1);
        harness.setLibrary(player2, List.of(new CloudfinRaptor(), new CloudfinRaptor(), new CloudfinRaptor()));

        resolveCombatAndTrigger();
        Card exiled = gd.getCardsExiledByPermanent(specter.getId()).getFirst();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cloudfin Raptor");
    }

    @Test
    @DisplayName("Nothing is exiled when the damaged player's library is empty")
    void noExileWhenLibraryEmpty() {
        Permanent specter = addAttackingSpecter(player1);
        harness.setLibrary(player2, List.of());

        resolveCombatAndTrigger();

        assertThat(gd.getCardsExiledByPermanent(specter.getId())).isEmpty();
    }

    @Test
    @DisplayName("No exile when the Specter is blocked and deals no combat damage to a player")
    void noExileWhenBlocked() {
        Permanent specter = addAttackingSpecter(player1);
        Permanent blocker = addCreatureReady(player2, new CloudfinRaptor());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLibrary(player2, List.of(new CloudfinRaptor(), new CloudfinRaptor(), new CloudfinRaptor()));

        resolveCombatAndTrigger();

        assertThat(gd.getCardsExiledByPermanent(specter.getId())).isEmpty();
    }

    @Test
    void controllerMayPlayExiledLandButStillHasNormalLandLimit() {
        Permanent specter = addAttackingSpecter(player1);
        Card land = new DimirGuildgate();
        harness.setLibrary(player2, List.of(land, new CloudfinRaptor()));
        resolveCombatAndTrigger();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Dimir Guildgate");
        assertThat(gd.getCardsExiledByPermanent(specter.getId())).isEmpty();
        harness.setHand(player1, List.of(new DimirGuildgate()));
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exiledCreatureRequiresCorrectManaColor() {
        Permanent specter = addAttackingSpecter(player1);
        Card card = new CloudfinRaptor();
        harness.setLibrary(player2, List.of(card, new CloudfinRaptor()));
        resolveCombatAndTrigger();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getCardsExiledByPermanent(specter.getId())).containsExactly(card);
    }

    @Test
    void exilePermissionDoesNotGrantFlash() {
        Permanent specter = addAttackingSpecter(player1);
        Card card = new CloudfinRaptor();
        harness.setLibrary(player2, List.of(card, new CloudfinRaptor()));
        resolveCombatAndTrigger();
        harness.forceStep(TurnStep.BEGIN_COMBAT);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getCardsExiledByPermanent(specter.getId())).containsExactly(card);
    }

    @Test
    void cardOwnerCannotUseSpectersExilePermission() {
        Permanent specter = addAttackingSpecter(player1);
        Card card = new CloudfinRaptor();
        harness.setLibrary(player2, List.of(card, new CloudfinRaptor()));
        resolveCombatAndTrigger();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player2, card.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getCardsExiledByPermanent(specter.getId())).containsExactly(card);
    }

    @Test
    void anotherSpecterCannotPlayCardsExiledWithDepartedSpecter() {
        Permanent specter = addAttackingSpecter(player1);
        Card card = new CloudfinRaptor();
        harness.setLibrary(player2, List.of(card, new CloudfinRaptor()));
        resolveCombatAndTrigger();
        harness.getPermanentRemovalService().removePermanentToHand(gd, specter);
        harness.addToBattlefield(player1, new NightveilSpecter());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    private Permanent addAttackingSpecter(Player player) {
        Permanent specter = addCreatureReady(player, new NightveilSpecter());
        specter.setAttacking(true);
        return specter;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
