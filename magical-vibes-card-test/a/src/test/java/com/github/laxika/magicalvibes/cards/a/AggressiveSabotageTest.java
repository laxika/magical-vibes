package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.PhyrexianRager;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AggressiveSabotage.class, PhyrexianRager.class})
class AggressiveSabotageTest extends BaseCardTest {

    @Test
    void targetPlayerDiscardsTwoCardsWithoutKicker() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new PhyrexianRager(), new PhyrexianRager(), new PhyrexianRager())));
        harness.setHand(player1, List.of(new AggressiveSabotage()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void kickedSpellAlsoDealsThreeDamageToTargetPlayer() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new PhyrexianRager(), new PhyrexianRager(), new PhyrexianRager())));
        harness.setHand(player1, List.of(new AggressiveSabotage()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castKickedInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    void cannotTargetAPermanent() {
        harness.addToBattlefield(player2, new PhyrexianRager());
        var permanentId = harness.getPermanentId(player2, "Phyrexian Rager");
        harness.setHand(player1, List.of(new AggressiveSabotage()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only target players");
    }

    @Test
    void kickedSpellDealsDamageEvenWhenTargetHasNoCards() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new AggressiveSabotage()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());
        int casterLifeBefore = gd.getLife(player1.getId());

        harness.castKickedInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(casterLifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void kickedSpellDiscardsOnlyAvailableCardThenDealsDamage() {
        harness.setHand(player2, List.of(new PhyrexianRager()));
        harness.setHand(player1, List.of(new AggressiveSabotage()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castKickedInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void kickedSpellCanTargetItsController() {
        harness.setHand(player1, List.of(new AggressiveSabotage(),
                new PhyrexianRager(), new PhyrexianRager(), new PhyrexianRager()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.castKickedInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof PhyrexianRager).hasSize(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }
    @Test
    void kickedDamageWaitsUntilBothDiscardsAreComplete() {
        harness.setHand(player2, List.of(new PhyrexianRager(),
                new PhyrexianRager(), new PhyrexianRager()));
        harness.setHand(player1, List.of(new AggressiveSabotage()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castKickedInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);

        harness.handleCardChosen(player2, 0);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);

        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    void cannotPayRedKickerWithOnlyBlackMana() {
        harness.setHand(player1, List.of(new AggressiveSabotage()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
