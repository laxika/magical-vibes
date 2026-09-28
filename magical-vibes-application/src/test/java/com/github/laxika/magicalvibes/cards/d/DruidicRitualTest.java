package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DruidicRitual.class, Forest.class, GrizzlyBears.class, Shock.class})
class DruidicRitualTest extends BaseCardTest {

    @Test
    void millsThreeAndReturnsUpToOneCreatureAndLand() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        List<Card> milled = List.of(new Shock(), new Shock(), new Shock());
        Card spell = new DruidicRitual();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setLibrary(player1, milled);
        harness.setHand(player1, List.of(spell));
        addMana();

        harness.castSorcery(player1, 0);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), land.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrderElementsOf(
                List.of(spell, milled.get(0), milled.get(1), milled.get(2)));
        assertThat(gd.playerHands.get(player1.getId())).contains(creature, land);
    }

    @Test
    void mayDeclineToMillAndSkipReturns() {
        Card creature = new GrizzlyBears();
        Card spell = new DruidicRitual();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.setHand(player1, List.of(spell));
        addMana();

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, spell);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
