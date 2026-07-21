package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoulquakeTest extends BaseCardTest {

    private void castSoulquake() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, new ArrayList<>(List.of(new Soulquake())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Returns all creatures on the battlefield to their owners' hands")
    void returnsAllCreaturesFromBattlefield() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());

        castSoulquake();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.CREATURE));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.CREATURE));

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName).contains("Serra Angel");
    }

    @Test
    @DisplayName("Returns all creature cards in graveyards to their owners' hands")
    void returnsCreatureCardsFromGraveyards() {
        Card myCreature = new GrizzlyBears();
        Card theirCreature = new SerraAngel();
        gd.playerGraveyards.get(player1.getId()).add(myCreature);
        gd.playerGraveyards.get(player2.getId()).add(theirCreature);

        castSoulquake();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName).contains("Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName).contains("Serra Angel");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(myCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(theirCreature);
    }

    @Test
    @DisplayName("Returns both battlefield creatures and graveyard creature cards at once")
    void returnsBattlefieldAndGraveyardTogether() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card graveyardCreature = new SerraAngel();
        gd.playerGraveyards.get(player2.getId()).add(graveyardCreature);

        castSoulquake();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName).contains("Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName).contains("Serra Angel");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.CREATURE));
    }

    @Test
    @DisplayName("Leaves non-creature permanents and non-creature graveyard cards untouched")
    void leavesNonCreaturesAlone() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        Card land = new Mountain();
        Card artifact = new DarksteelRelic();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(land, artifact));

        castSoulquake();

        harness.assertOnBattlefield(player1, "Glorious Anthem");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land, artifact);
    }

    @Test
    @DisplayName("Works with empty battlefields and graveyards (no crash)")
    void worksWithEmptyState() {
        castSoulquake();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Soulquake"));
    }
}
