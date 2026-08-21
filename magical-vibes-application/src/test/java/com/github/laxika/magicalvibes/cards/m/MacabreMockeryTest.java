package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MacabreMockeryTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an opponent's creature with haste and +2/+0, then sacrifices it at the next end step")
    void returnsCreatureWithTemporaryRiders() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new MacabreMockery()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1.getId(), target.getId());
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(returned.getPowerModifier()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Requires a creature card in an opponent's graveyard")
    void rejectsInvalidGraveyardTargets() {
        Card target = new Pacifism();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new MacabreMockery()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature card");
    }

    @Test
    @DisplayName("Cannot target a creature card in your own graveyard")
    void rejectsOwnGraveyardTarget() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new MacabreMockery()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent's graveyard");
    }

    private Permanent findPermanent(UUID playerId, UUID cardId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Returned permanent not found on battlefield"));
    }
}
