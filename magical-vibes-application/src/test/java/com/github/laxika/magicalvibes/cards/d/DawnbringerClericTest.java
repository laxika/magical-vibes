package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnbringerCleric.class, GrizzlyBears.class, GloriousAnthem.class})
class DawnbringerClericTest extends BaseCardTest {

    @Test
    void cureWoundsGainsTwoLife() {
        int lifeBefore = gd.getLife(player1.getId());
        castCleric();

        harness.handleListChoice(player1, "Cure Wounds — You gain 2 life.");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void dispelMagicDestroysTargetEnchantment() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        castCleric();

        harness.handleListChoice(player1, "Dispel Magic — Destroy target enchantment.");
        harness.handlePermanentChosen(player1, anthem.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    void gentleReposeExilesTargetGraveyardCard() {
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCard));
        castCleric();

        harness.handleListChoice(player1, "Gentle Repose — Exile target card from a graveyard.");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(graveyardCard);
    }

    private void castCleric() {
        harness.setHand(player1, List.of(new DawnbringerCleric()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }
}
