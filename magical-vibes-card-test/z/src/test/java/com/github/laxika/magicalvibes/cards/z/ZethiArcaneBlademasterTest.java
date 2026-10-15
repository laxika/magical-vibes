package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

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

    @Test
    void attackingCopiesOwnedKickCounterCardsFromAnEarlierZethi() {
        addCreatureReady(player1, new ZethiArcaneBlademaster());
        DarkRitual ritual = new DarkRitual();
        gd.addToExile(player1.getId(), ritual, java.util.UUID.randomUUID());
        gd.exiledCardsWithKickCounters.add(ritual.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);

        declareAttackers(player1, List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Dark Ritual"));
        assertThat(gd.findExiledCard(ritual.getId())).isNotNull();
    }

    @Test
    void enteringWithoutKickingLeavesInstantsInTheGraveyard() {
        DarkRitual ritual = new DarkRitual();
        harness.setGraveyard(player1, List.of(ritual));
        harness.setHand(player1, List.of(new ZethiArcaneBlademaster()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        castZethi(List.of());
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MultiGraveyardChoice) {
            harness.handleMultipleCardsChosen(player1, List.of());
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ritual);
        assertThat(gd.findExiledCard(ritual.getId())).isNull();
    }

    @Test
    void kickedEntryMayChooseNoCards() {
        DarkRitual ritual = new DarkRitual();
        harness.setGraveyard(player1, List.of(ritual));
        harness.setHand(player1, List.of(new ZethiArcaneBlademaster()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        castZethi(List.of("{W/U}"));
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ritual);
        assertThat(gd.findExiledCard(ritual.getId())).isNull();
    }

    @Test
    void decliningTheCopyKeepsTheOriginalExiledWithItsCounter() {
        addCreatureReady(player1, new ZethiArcaneBlademaster());
        DarkRitual ritual = new DarkRitual();
        gd.addToExile(player1.getId(), ritual, gd.playerBattlefields.get(player1.getId()).getFirst().getId());
        gd.exiledCardsWithKickCounters.add(ritual.getId());

        declareAttackers(player1, List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Dark Ritual"));
        assertThat(gd.findExiledCard(ritual.getId())).isNotNull();
        assertThat(gd.exiledCardsWithKickCounters).contains(ritual.getId());
    }

    @Test
    void twoKicksCanExileTwoInstantsButNotAnOpponentsInstant() {
        DarkRitual first = new DarkRitual();
        DarkRitual second = new DarkRitual();
        DarkRitual opponents = new DarkRitual();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setGraveyard(player2, List.of(opponents));
        harness.setHand(player1, List.of(new ZethiArcaneBlademaster()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        castZethi(List.of("{W/U}", "{W/U}"));
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.cards()).containsExactlyInAnyOrder(first, second);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponents);
        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
        assertThat(gd.exiledCardsWithKickCounters).contains(first.getId(), second.getId());
    }

    @Test
    void attackingDoesNotCopyUnmarkedOrOpponentOwnedCards() {
        addCreatureReady(player1, new ZethiArcaneBlademaster());
        java.util.UUID sourceId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        DarkRitual unmarked = new DarkRitual();
        DarkRitual opponents = new DarkRitual();
        gd.addToExile(player1.getId(), unmarked, sourceId);
        gd.addToExile(player2.getId(), opponents, sourceId);
        gd.exiledCardsWithKickCounters.add(opponents.getId());

        declareAttackers(player1, List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Dark Ritual"));
        assertThat(gd.findExiledCard(unmarked.getId())).isNotNull();
        assertThat(gd.findExiledCard(opponents.getId())).isNotNull();
    }

    private void castZethi(List<String> payments) {
        harness.castCreatureWithRepeatedCosts(player1, 0, payments);
        harness.passBothPriorities();
    }
}
