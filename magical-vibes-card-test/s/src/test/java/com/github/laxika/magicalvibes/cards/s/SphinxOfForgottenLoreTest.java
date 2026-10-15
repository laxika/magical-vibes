package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SphinxOfForgottenLore.class, Shock.class, GrizzlyBears.class, LavaAxe.class})
class SphinxOfForgottenLoreTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking targets an instant or sorcery card in its controller's graveyard")
    void attackTargetsInstantOrSorcery() {
        Card instant = new Shock();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(instant, creature));
        addReadySphinx();

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(instant.getId());

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).containsExactly(instant.getId());
    }

    @Test
    @DisplayName("Granted flashback uses the card's mana cost and exiles the card after casting")
    void grantsFlashbackWithManaCost() {
        Card instant = new Shock();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(instant));
        addReadySphinx();

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveFlashback(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(instant.getId()));
    }

    @Test
    @DisplayName("The attack trigger does not trigger without an instant or sorcery in its controller's graveyard")
    void noLegalTargetSkipsTrigger() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Shock()));
        addReadySphinx();

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sorceryRetainsNormalTimingAndUsesItsManaCost() {
        Card sorcery = new LavaAxe();
        harness.setGraveyard(player1, List.of(sorcery));
        addReadySphinx();
        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Lava Axe");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(5);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        int lifeBefore = gd.getLife(player2.getId());
        harness.castAndResolveFlashback(player1, 0, player2.getId());

        harness.assertLife(player2, lifeBefore - 5);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(sorcery);
    }

    @Test
    void removedGraveyardTargetDoesNotGainFlashback() {
        Card instant = new Shock();
        harness.setGraveyard(player1, List.of(instant));
        addReadySphinx();
        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).doesNotContain(instant.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void grantSurvivesSphinxLeavingBattlefield() {
        Card instant = new Shock();
        harness.setGraveyard(player1, List.of(instant));
        Permanent sphinx = addReadySphinx();
        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(sphinx);
        harness.setGraveyard(player1, List.of(instant, sphinx.getCard()));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        harness.assertLife(player2, lifeBefore - 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(instant);
    }

    @Test
    void flashbackGrantExpiresAtEndOfTurn() {
        Card instant = new Shock();
        harness.setGraveyard(player1, List.of(instant));
        addReadySphinx();
        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(instant);
    }

    @Test
    void flashAllowsCastingDuringOpponentsUpkeep() {
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.castFromHand(player1, new SphinxOfForgottenLore(), "{2}{U}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sphinx of Forgotten Lore");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadySphinx() {
        return addCreatureReady(player1, new SphinxOfForgottenLore());
    }
}
