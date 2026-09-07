package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarnesserOfStorms.class, GrizzlyBears.class, Mountain.class, Shock.class})
class HarnesserOfStormsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell exiles the top card and lets you play it this turn")
    void noncreatureSpellExilesTopCardForTheTurn() {
        harness.addToBattlefield(player1, new HarnesserOfStorms());
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        gs.playCardFromExile(gd, player1, topCard.getId(), null, null);
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("The ability triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new HarnesserOfStorms());
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A non-Otter creature spell does not trigger the ability")
    void nonOtterCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new HarnesserOfStorms());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Casting an Otter spell triggers the ability")
    void otterSpellTriggers() {
        harness.addToBattlefield(player1, new HarnesserOfStorms());
        harness.setHand(player1, List.of(new HarnesserOfStorms()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }
}
