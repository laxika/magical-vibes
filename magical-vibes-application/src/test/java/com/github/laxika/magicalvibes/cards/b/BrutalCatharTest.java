package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoonrageBrute;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrutalCathar.class, MoonrageBrute.class, GrizzlyBears.class, Shock.class})
class BrutalCatharTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by exiling a creature an opponent controls")
    void etbExilesOpponentCreatureUntilCatharLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        Permanent cathar = castCathar(bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears.getCard());

        resetForFollowUpSpell();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, cathar.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature controlled by its controller")
    void cannotTargetOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BrutalCathar()));
        addCatharMana(player1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Transforms into Moonrage Brute when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        Permanent cathar = addCathar();
        gd.spellsCastLastTurn.clear();

        advanceToUpkeepAndResolveTransform(player1);

        assertThat(cathar.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        Permanent cathar = addCathar();
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cathar.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Transforms back and exiles a new opposing creature")
    void transformsBackAndExilesCreature() {
        Permanent cathar = addCathar();
        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolveTransform(player1);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceToUpkeepAndResolveTransform(player2);

        assertThat(cathar.isTransformed()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent addCathar() {
        return harness.addToBattlefieldAndReturn(player1, new BrutalCathar());
    }

    private Permanent castCathar(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new BrutalCathar()));
        addCatharMana(player1);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Brutal Cathar");
    }

    private void addCatharMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private void advanceToUpkeepAndResolveTransform(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void resetForFollowUpSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
