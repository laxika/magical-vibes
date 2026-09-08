package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KarmicGuideTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a targeted creature card from your graveyard to the battlefield")
    void etbReturnsCreatureFromGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        castAndResolveGuide(creature);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB cannot target a noncreature card in your graveyard")
    void etbCannotTargetNoncreatureCard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new KarmicGuide()));
        addGuideMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Holy Day");
    }

    @Test
    @DisplayName("Declining echo sacrifices Karmic Guide at the next upkeep")
    void decliningEchoSacrificesGuide() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        castAndResolveGuide(creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Karmic Guide");
        harness.assertInGraveyard(player1, "Karmic Guide");
    }

    @Test
    @DisplayName("Paying echo keeps Karmic Guide and echo does not trigger again")
    void payingEchoKeepsGuideAndIsOneShot() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        castAndResolveGuide(creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        addEchoMana();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Karmic Guide");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Karmic Guide");
    }

    private void castAndResolveGuide(Card target) {
        harness.setHand(player1, List.of(new KarmicGuide()));
        addGuideMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
    }

    private void addGuideMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }

    private void addEchoMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
