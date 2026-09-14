package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZethiArcaneBlademaster.class, Fog.class, Forest.class, DarkRitual.class})
class ZethiArcaneBlademasterTest extends BaseCardTest {

    @Test
    void exilesUpToTheNumberOfMultikickerPaymentsAndAddsKickCounters() {
        Card instant = new Fog();
        Card nonInstant = new Forest();
        harness.setGraveyard(player1, List.of(instant, nonInstant));
        harness.setHand(player1, List.of(new ZethiArcaneBlademaster()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        castZethi(List.of("{W/U}", "{W/U}"));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice)
                .withFailMessage("interaction=%s stack=%s graveyard=%s exiled=%s", gd.interaction.activeInteraction(),
                        gd.stack, gd.playerGraveyards.get(player1.getId()), gd.exiledCards)
                .isNotNull();
        assertThat(choice.cards()).containsExactly(instant);
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonInstant);
        assertThat(gd.findExiledCard(instant.getId())).isNotNull();
        assertThat(gd.exiledCardsWithKickCounters).contains(instant.getId());
    }

    @Test
    void attackingOffersOwnedKickCounterCardsAsNormalCostCopies() {
        addCreatureReady(player1, new ZethiArcaneBlademaster());
        DarkRitual ritual = new DarkRitual();
        gd.addToExile(player1.getId(), ritual, gd.playerBattlefields.get(player1.getId()).getFirst().getId());
        gd.exiledCardsWithKickCounters.add(ritual.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThat(gd.getCardsExiledByPermanent(gd.playerBattlefields.get(player1.getId()).getFirst().getId()))
                .containsExactly(ritual);

        declareAttackers(player1, List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .withFailMessage("interaction=%s stack=%s pending=%s exiled=%s", gd.interaction.activeInteraction(),
                        gd.stack, gd.pendingMayAbilities, gd.exiledCards)
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Dark Ritual"));
    }

    private void castZethi(List<String> payments) {
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                payments, false);
        harness.passBothPriorities();
    }
}
