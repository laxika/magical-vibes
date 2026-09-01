package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GhastlyConscriptionTest extends BaseCardTest {

    @Test
    void exilesCreatureCardsAndManifestsThemUnderTheSpellController() {
        Card bears = new GrizzlyBears();
        Card plains = new Plains();
        harness.setGraveyard(player2, List.of(bears, plains, new GrizzlyBears()));
        harness.setHand(player1, List.of(new GhastlyConscription()));
        addMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(plains.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested)
                .hasSize(2)
                .allMatch(Permanent::isFaceDown);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(Permanent::isManifested);
    }

    @Test
    void doesNothingWhenTargetGraveyardHasNoCreatureCards() {
        Card plains = new Plains();
        harness.setGraveyard(player2, List.of(plains));
        harness.setHand(player1, List.of(new GhastlyConscription()));
        addMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(plains.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
    }

    @Test
    void cannotTargetAPermanent() {
        Permanent permanent = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhastlyConscription()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
