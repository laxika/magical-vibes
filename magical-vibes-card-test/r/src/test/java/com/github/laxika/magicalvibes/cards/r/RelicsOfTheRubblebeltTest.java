package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.d.DimirSignet;
import com.github.laxika.magicalvibes.cards.g.GolgariSignet;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.cards.i.IzzetSignet;
import com.github.laxika.magicalvibes.cards.o.OrzhovSignet;
import com.github.laxika.magicalvibes.cards.r.RakdosSignet;
import com.github.laxika.magicalvibes.cards.s.SelesnyaSignet;
import com.github.laxika.magicalvibes.cards.s.SimicSignet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RelicsOfTheRubblebelt.class, AzoriusSignet.class, BorosSignet.class,
        DimirSignet.class, GolgariSignet.class, GruulSignet.class, IzzetSignet.class,
        OrzhovSignet.class, RakdosSignet.class, SelesnyaSignet.class, SimicSignet.class})
class RelicsOfTheRubblebeltTest extends BaseCardTest {

    @Test
    void draftsTwiceAndPutsOneDraftedSignetOntoTheBattlefieldTapped() {
        harness.setHand(player1, List.of(new RelicsOfTheRubblebelt()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.DraftTwiceSpellbookChoice firstDraft =
                gd.interaction.activeInteraction(PendingInteraction.DraftTwiceSpellbookChoice.class);
        assertThat(firstDraft).isNotNull();
        assertThat(firstDraft.cards()).hasSize(3);

        Card firstChoice = firstDraft.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(firstChoice.getId()));

        PendingInteraction.DraftTwiceSpellbookChoice secondDraft =
                gd.interaction.activeInteraction(PendingInteraction.DraftTwiceSpellbookChoice.class);
        assertThat(secondDraft).isNotNull();
        assertThat(secondDraft.draftedCards()).containsExactly(firstChoice);
        assertThat(secondDraft.cards()).hasSize(3);

        Card secondChoice = secondDraft.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(secondChoice.getId()));

        PendingInteraction.RevealedHandChoice battlefieldChoice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(battlefieldChoice).isNotNull();
        assertThat(battlefieldChoice.validIndices()).hasSize(2);

        int selectedHandIndex = battlefieldChoice.validIndices().getFirst();
        UUID selectedCardId = gd.playerHands.get(player1.getId()).get(selectedHandIndex).getId();
        harness.handleCardChosen(player1, selectedHandIndex);

        Permanent draftedPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(selectedCardId))
                .findFirst().orElseThrow();
        assertThat(draftedPermanent.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
