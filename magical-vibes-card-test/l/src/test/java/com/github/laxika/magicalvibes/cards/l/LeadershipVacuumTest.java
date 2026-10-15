package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AnjeFalkenrath;
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

@CardUsed({LeadershipVacuum.class, GrizzlyBears.class, AnjeFalkenrath.class})
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
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new LeadershipVacuum()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

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
                .hasMessageContaining("only target players");
    }

    @Test
    void drawsForCasterWhenTargetControlsNoCommanders() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new LeadershipVacuum()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
    }

    @Test
    void canTargetSelfAndLeavesOtherPlayersCommanderOnBattlefield() {
        gd.format = DeckFormat.COMMANDER;
        Card ownCommander = new AnjeFalkenrath();
        ownCommander.setOwnerId(player1.getId());
        gd.makeCommander(player1.getId(), ownCommander);
        Card otherCommander = new AnjeFalkenrath();
        otherCommander.setOwnerId(player2.getId());
        gd.makeCommander(player2.getId(), otherCommander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>());
        gd.playerCommandZones.put(player2.getId(), new ArrayList<>());
        harness.addToBattlefield(player1, ownCommander);
        var untouchedCommander = harness.addToBattlefieldAndReturn(player2, otherCommander);
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new LeadershipVacuum()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerCommandZones.get(player1.getId())).containsExactly(ownCommander);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(untouchedCommander);
        assertThat(gd.playerCommandZones.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.pendingCommanderZoneMoves).isEmpty();
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
