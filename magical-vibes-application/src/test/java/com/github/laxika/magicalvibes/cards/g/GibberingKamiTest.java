package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.k.KamiOfTheHunt;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.m.MossKami;
import com.github.laxika.magicalvibes.cards.r.RendSpirit;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GibberingKami.class, RendSpirit.class, LanternKami.class, KamiOfTheHunt.class,
        SakuraTribeElder.class, MossKami.class})
class GibberingKamiTest extends BaseCardTest {

    /** Destroys Gibbering Kami so its soulshift trigger is collected. */
    private void killKami() {
        harness.setHand(player1, List.of(new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Gibbering Kami"));
    }

    @Test
    @DisplayName("Soulshift 3 returns a targeted Spirit with mana value 3 or less from your graveyard to your hand")
    void deathReturnsCheapSpiritToHand() {
        harness.addToBattlefield(player1, new GibberingKami());
        Card kami = new LanternKami();
        harness.setGraveyard(player1, List.of(kami));

        killKami();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(kami.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lantern Kami");
        harness.assertNotInGraveyard(player1, "Lantern Kami");
    }

    @Test
    @DisplayName("Soulshift may be declined")
    void soulshiftCanBeDeclined() {
        harness.addToBattlefield(player1, new GibberingKami());
        Card eligible = new LanternKami();
        harness.setGraveyard(player1, List.of(eligible));

        killKami();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lantern Kami");
        harness.assertNotInHand(player1, "Lantern Kami");
    }

    @Test
    @DisplayName("Spirits with mana value 4 or greater and an opponent's Spirits are not legal targets")
    void expensiveOrOpponentSpiritNotTargetable() {
        harness.addToBattlefield(player1, new GibberingKami());
        Card cheapSpirit = new LanternKami();
        Card boundarySpirit = new KamiOfTheHunt();
        Card nonSpirit = new SakuraTribeElder();
        Card expensiveSpirit = new MossKami();
        Card opponentSpirit = new LanternKami();
        harness.setGraveyard(player1, List.of(cheapSpirit, boundarySpirit, nonSpirit, expensiveSpirit));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        killKami();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(cheapSpirit.getId(), boundarySpirit.getId());
        assertThat(choice.validCardIds()).doesNotContain(nonSpirit.getId(), expensiveSpirit.getId(), opponentSpirit.getId());
    }

    @Test
    @DisplayName("With no Spirit with mana value 3 or less in your graveyard the trigger presents no choice")
    void noLegalSpiritNoChoice() {
        harness.addToBattlefield(player1, new GibberingKami());
        harness.setGraveyard(player1, List.of(new MossKami()));

        killKami();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
