package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExileIntoDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Target player sacrifices a creature with mana value 3 or less")
    void sacrificesEligibleCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        harness.setHand(player1, List.of(new ExileIntoDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does nothing when the target player controls no eligible creature")
    void noEligibleCreatureNoSacrifice() {
        harness.addToBattlefield(player2, new GiantSpider());

        harness.setHand(player1, List.of(new ExileIntoDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player1, "Exile into Darkness");
    }

    @Test
    @DisplayName("Returns itself from the graveyard when the controller has more cards in hand")
    void returnsFromGraveyardWithHandAdvantage() {
        ExileIntoDarkness card = new ExileIntoDarkness();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Exile into Darkness");
        harness.assertNotInGraveyard(player1, "Exile into Darkness");
    }

    @Test
    @DisplayName("Does not trigger from the graveyard without hand advantage")
    void doesNotReturnWithoutHandAdvantage() {
        harness.setGraveyard(player1, List.of(new ExileIntoDarkness()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
