package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhenWillYouLearn.class, Forest.class, GrizzlyBears.class})
class WhenWillYouLearnTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles each opponent's top card and offers only the exiled spell for free")
    void exilesEachOpponentsTopCardAndOffersOnlyTheSpell() {
        GrizzlyBears spell = new GrizzlyBears();
        Forest land = new Forest();
        Forest remainder = new Forest();
        harness.setLibrary(player2, List.of(spell, land, remainder));

        resolveScheme();

        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice.validCardIds()).containsExactly(spell.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(spell);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(land, remainder);
    }

    @Test
    @DisplayName("Casts any selected exiled spell without paying its mana cost")
    void castsSelectedExiledSpellForFree() {
        GrizzlyBears spell = new GrizzlyBears();
        Forest remainder = new Forest();
        harness.setLibrary(player2, List.of(spell, remainder));

        resolveScheme();
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == spell
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL
                && entry.getControllerId().equals(player1.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(remainder);
    }

    private void resolveScheme() {
        Card scheme = new WhenWillYouLearn();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL)));
        harness.passBothPriorities();
    }
}
