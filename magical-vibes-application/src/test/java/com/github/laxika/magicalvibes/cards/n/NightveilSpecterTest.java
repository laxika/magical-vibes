package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NightveilSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player exiles the top card of that player's library, tracked with the Specter")
    void combatDamageExilesTopCardOfDamagedPlayerLibrary() {
        Permanent specter = addAttackingSpecter(player1);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        resolveCombatAndTrigger();

        assertThat(gd.getCardsExiledByPermanent(specter.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The exiled card is exiled face up")
    void exiledCardIsFaceUp() {
        Permanent specter = addAttackingSpecter(player1);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        resolveCombatAndTrigger();

        assertThat(gd.exiledCards).filteredOn(e -> specter.getId().equals(e.sourcePermanentId()))
                .isNotEmpty()
                .noneMatch(ExiledCardEntry::faceDown);
    }

    @Test
    @DisplayName("The Specter's controller may cast a card exiled with it, paying its normal cost")
    void controllerMayCastExiledCard() {
        Permanent specter = addAttackingSpecter(player1);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        resolveCombatAndTrigger();
        Card exiled = gd.getCardsExiledByPermanent(specter.getId()).getFirst();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
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
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        resolveCombatAndTrigger();

        assertThat(gd.getCardsExiledByPermanent(specter.getId())).isEmpty();
    }

    private Permanent addAttackingSpecter(Player player) {
        Permanent specter = addCreatureReady(player, new NightveilSpecter());
        specter.setAttacking(true);
        return specter;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
