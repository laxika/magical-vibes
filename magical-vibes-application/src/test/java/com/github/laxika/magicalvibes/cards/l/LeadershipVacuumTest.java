package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeadershipVacuum.class, GrizzlyBears.class})
class LeadershipVacuumTest extends BaseCardTest {

    @Test
    void returnsAllCommandersTargetPlayerControlsToTheirOwnersCommandZonesAndDraws() {
        Card ownCommander = commander(player1, "Own Commander");
        Card opposingCommander = commander(player2, "Opposing Commander");
        gd.playerCommandZones.get(player1.getId()).clear();
        gd.playerCommandZones.get(player2.getId()).clear();
        harness.addToBattlefield(player2, ownCommander);
        harness.addToBattlefield(player2, opposingCommander);
        var nonCommander = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        Card drawnCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(drawnCard);
        harness.setHand(player1, List.of(new LeadershipVacuum()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(nonCommander);
        assertThat(gd.playerCommandZones.get(player1.getId())).containsExactly(ownCommander);
        assertThat(gd.playerCommandZones.get(player2.getId())).containsExactly(opposingCommander);
        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(handSizeBefore)
                .contains(drawnCard);
        assertThat(gd.pendingCommanderZoneMoves).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotTargetAPermanent() {
        var permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LeadershipVacuum()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, permanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");
    }

    private Card commander(com.github.laxika.magicalvibes.model.Player owner, String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        card.setManaCost("{1}");
        card.setPower(2);
        card.setToughness(2);
        card.setOwnerId(owner.getId());
        card.freeze();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(owner.getId(), card);
        gd.playerCommandZones.put(owner.getId(), new ArrayList<>(List.of(card)));
        return card;
    }
}
