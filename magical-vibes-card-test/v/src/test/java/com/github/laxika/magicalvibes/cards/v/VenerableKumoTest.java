package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.h.HideousLaughter;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VenerableKumo.class, LanternKami.class, KamiOfOldStone.class, HideousLaughter.class})
class VenerableKumoTest extends BaseCardTest {

    /** Venerable Kumo is 2/3, so one marked damage makes Hideous Laughter lethal. */
    private void killKumoWithHideousLaughter() {
        findPermanent(player1, "Venerable Kumo").setMarkedDamage(1);
        harness.castFromHand(player1, new HideousLaughter(), "{2}{B}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Soulshift 4 returns a targeted Spirit with mana value 4 or less from your graveyard to your hand")
    void deathReturnsCheapSpiritToHand() {
        harness.addToBattlefield(player1, new VenerableKumo());
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, List.of(spirit));

        killKumoWithHideousLaughter();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Lantern Kami");
        harness.assertNotInGraveyard(player1, "Lantern Kami");
    }

    @Test
    @DisplayName("Only your Spirit cards with mana value 4 or less are legal targets")
    void onlyOwnSpiritsAtOrBelowLimitAreTargetable() {
        harness.addToBattlefield(player1, new VenerableKumo());
        Card cheapSpirit = new LanternKami();
        Card boundarySpirit = new KamiOfOldStone();
        Card expensiveSpirit = new VenerableKumo();
        Card nonSpirit = new HideousLaughter();
        Card opponentSpirit = new LanternKami();
        harness.setGraveyard(player1, List.of(cheapSpirit, boundarySpirit, expensiveSpirit, nonSpirit));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        killKumoWithHideousLaughter();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(cheapSpirit.getId(), boundarySpirit.getId());
        assertThat(choice.validCardIds()).doesNotContain(expensiveSpirit.getId(), nonSpirit.getId(), opponentSpirit.getId());
    }

    @Test
    @DisplayName("Soulshift may be declined")
    void soulshiftMayBeDeclined() {
        harness.addToBattlefield(player1, new VenerableKumo());
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, List.of(spirit));

        killKumoWithHideousLaughter();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Lantern Kami");
        harness.assertNotInHand(player1, "Lantern Kami");
    }

    @Test
    @DisplayName("With no Spirit with mana value 4 or less in your graveyard the trigger presents no choice")
    void noLegalSpiritNoChoice() {
        harness.addToBattlefield(player1, new VenerableKumo());
        harness.setGraveyard(player1, List.of(new VenerableKumo()));

        killKumoWithHideousLaughter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Reach allows Venerable Kumo to block a flying creature")
    void blocksFlyingCreature() {
        addCreatureReady(player1, new LanternKami());
        var kumo = addCreatureReady(player2, new VenerableKumo());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(kumo.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Soulshift returns a Spirit at the mana value four boundary")
    void returnsSpiritAtManaValueLimit() {
        harness.addToBattlefield(player1, new VenerableKumo());
        Card spirit = new KamiOfOldStone();
        harness.setGraveyard(player1, List.of(spirit));

        killKumoWithHideousLaughter();
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Kami of Old Stone");
        harness.assertNotInGraveyard(player1, "Kami of Old Stone");
    }
}
