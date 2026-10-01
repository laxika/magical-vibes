package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AncientImperiosaur;
import com.github.laxika.magicalvibes.cards.c.ConclaveTribunal;
import com.github.laxika.magicalvibes.cards.k.KnightErrantOfEos;
import com.github.laxika.magicalvibes.cards.l.LoxodonRestorer;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMultitudes;
import com.github.laxika.magicalvibes.cards.n.NissasExpedition;
import com.github.laxika.magicalvibes.cards.o.Overwhelm;
import com.github.laxika.magicalvibes.cards.t.TriplicateSpirits;
import com.github.laxika.magicalvibes.cards.v.VeneratedLoxodon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmmaraVoiceOfTheConclave.class, AncientImperiosaur.class, ConclaveTribunal.class,
        KnightErrantOfEos.class, LoxodonRestorer.class, MarchOfTheMultitudes.class,
        NissasExpedition.class, Overwhelm.class, TriplicateSpirits.class, VeneratedLoxodon.class})
class EmmaraVoiceOfTheConclaveTest extends BaseCardTest {

    private static final Set<String> SPELLBOOK = Set.of(
            "Ancient Imperiosaur", "Conclave Tribunal", "Knight-Errant of Eos",
            "Loxodon Restorer", "March of the Multitudes", "Nissa's Expedition",
            "Overwhelm", "Triplicate Spirits", "Venerated Loxodon");

    @Test
    void entersAndOffersThreeCardsFromItsSpellbook() {
        harness.enterBattlefieldAndReturn(player1, new EmmaraVoiceOfTheConclave());
        resolveAllTriggers();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);
        assertThat(choice.cards()).extracting(Card::getName).allMatch(SPELLBOOK::contains);

        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
    }
}
