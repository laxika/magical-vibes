package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlamesOfMoradin.class, SolRing.class, GrizzlyBears.class})
class FlamesOfMoradinTest extends BaseCardTest {

    @Test
    void destroysArtifactsAndConjuresModifiedNontokenCopies() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Card tokenCard = new SolRing();
        tokenCard.setToken(true);
        Permanent tokenArtifact = harness.addToBattlefieldAndReturn(player1, tokenCard);
        harness.setHand(player1, List.of(new FlamesOfMoradin()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of(artifact.getId(), tokenArtifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Sol Ring", "Flames of Moradin")
                .hasSize(2);
        List<Card> copies = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Sol Ring"))
                .toList();
        assertThat(copies).hasSize(1);
        assertThat(copies.getFirst().getId()).isNotEqualTo(artifact.getCard().getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castWithAlternateCost(player1,
                gd.playerHands.get(player1.getId()).indexOf(copies.getFirst()), List.of());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getName)
                .containsExactly("Sol Ring");

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void onlyArtifactsCanBeTargeted() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlamesOfMoradin()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }
}
