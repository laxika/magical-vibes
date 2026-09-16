package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RotwidowPack.class, GiantSpider.class, GrizzlyBears.class})
class RotwidowPackTest extends BaseCardTest {

    @Test
    void activatedAbilityPromptsForCreatureCardToExile() {
        harness.addToBattlefield(player1, new RotwidowPack());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
    }

    @Test
    void activatedAbilityCreatesSpiderAndEachOpponentLosesForAllControlledSpiders() {
        harness.addToBattlefield(player1, new RotwidowPack());
        harness.addToBattlefield(player1, new GiantSpider());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        Permanent token = findPermanent(player1, "Spider");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SPIDER);
        assertThat(token.getCard().getKeywords()).contains(Keyword.REACH);
    }

    @Test
    void activatedAbilityRequiresCreatureCardInGraveyard() {
        harness.addToBattlefield(player1, new RotwidowPack());
        harness.setGraveyard(player1, List.of());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
