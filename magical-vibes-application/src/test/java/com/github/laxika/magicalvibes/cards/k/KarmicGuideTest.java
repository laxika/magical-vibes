package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AngelicCurator;
import com.github.laxika.magicalvibes.cards.b.BlessedReversal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KarmicGuide.class, AngelicCurator.class, BlessedReversal.class})
class KarmicGuideTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a targeted creature card from your graveyard to the battlefield")
    void etbReturnsCreatureFromGraveyard() {
        Card creature = new AngelicCurator();
        harness.setGraveyard(player1, List.of(creature));

        castAndResolveGuide(creature);

        harness.assertOnBattlefield(player1, "Angelic Curator");
        harness.assertNotInGraveyard(player1, "Angelic Curator");
    }

    @Test
    @DisplayName("ETB cannot target a noncreature card in your graveyard")
    void etbCannotTargetNoncreatureCard() {
        Card instant = new BlessedReversal();
        harness.setGraveyard(player1, List.of(instant));

        harness.castFromHand(player1, new KarmicGuide(), "{3}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Blessed Reversal");
    }

    @Test
    @DisplayName("ETB cannot target a creature card in an opponent's graveyard")
    void etbCannotTargetOpponentsGraveyard() {
        Card creature = new AngelicCurator();
        harness.setGraveyard(player2, List.of(creature));

        harness.castFromHand(player1, new KarmicGuide(), "{3}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Angelic Curator");
        harness.assertOnBattlefield(player1, "Karmic Guide");
    }

    @Test
    @DisplayName("ETB does nothing if its targeted creature card leaves the graveyard before resolution")
    void etbFizzesIfTargetLeavesGraveyardBeforeResolution() {
        Card creature = new AngelicCurator();
        harness.setGraveyard(player1, List.of(creature));

        harness.castFromHand(player1, new KarmicGuide(), "{3}{W}{W}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Karmic Guide");
        harness.assertNotOnBattlefield(player1, "Angelic Curator");
    }

    @Test
    @DisplayName("Declining echo sacrifices Karmic Guide at the next upkeep")
    void decliningEchoSacrificesGuide() {
        Card creature = new AngelicCurator();
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
        Card creature = new AngelicCurator();
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
        harness.castFromHand(player1, new KarmicGuide(), "{3}{W}{W}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
    }

    private void addEchoMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
