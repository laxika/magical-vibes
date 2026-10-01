package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AgentOfRaffine.class, Forest.class, Shock.class})
class AgentOfRaffineTest extends BaseCardTest {

    @Test
    void conjuresTheTopCardThenExilesThatCardFaceDown() {
        harness.setHand(player1, List.of());
        addReadyAgent(player1);
        Shock topCard = new Shock();
        Forest nextCard = new Forest();
        harness.setLibrary(player2, List.of(topCard, nextCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(duplicate.getName()).isEqualTo(topCard.getName());
        assertThat(duplicate.getId()).isNotEqualTo(topCard.getId());
        assertThat(duplicate.getOwnerId()).isEqualTo(player1.getId());
        assertThat(gd.perpetualAnyColorManaForCastCardIds).contains(duplicate.getId());

        ExiledCardEntry exiled = gd.findExiledCard(topCard.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.faceDown()).isTrue();
        assertThat(exiled.ownerId()).isEqualTo(player2.getId());
        assertThat(exiled.exilerId()).isEqualTo(player2.getId());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void canOnlyTargetAnOpponent() {
        addReadyAgent(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAgent(Player player) {
        Permanent agent = addCreatureReady(player, new AgentOfRaffine());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return agent;
    }
}
