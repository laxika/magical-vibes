package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CabalTherapy.class, GrizzlyBears.class, Shock.class, Forest.class})
class CabalTherapyTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses a nonland name and discards every matching card from the target's hand")
    void discardsEveryMatchingCard() {
        Card firstBears = new GrizzlyBears();
        Card secondBears = new GrizzlyBears();
        Card shock = new Shock();
        Card forest = new Forest();
        castFromHand(new ArrayList<>(List.of(firstBears, secondBears, shock, forest)));

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("Grizzly Bears", "Shock").doesNotContain("Forest");

        harness.handleListChoice(player1, "Grizzly Bears");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(shock, forest);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(firstBears, secondBears);
    }

    @Test
    @DisplayName("Does not discard cards when the chosen name is absent from the target's hand")
    void absentNameDoesNothing() {
        Card shock = new Shock();
        Card forest = new Forest();
        harness.setHand(player1, List.of(new CabalTherapy(), new GrizzlyBears()));
        harness.setHand(player2, new ArrayList<>(List.of(shock, forest)));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Grizzly Bears");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(shock, forest);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Flashback sacrifices a creature and exiles Cabal Therapy after it resolves")
    void flashbackSacrificesCreatureAndExilesSpell() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        CabalTherapy therapy = new CabalTherapy();
        harness.setGraveyard(player1, List.of(therapy));
        harness.setHand(player2, List.of(new Shock()));

        harness.castFlashbackWithSacrifice(player1, 0, player2.getId(), creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Shock");

        harness.assertNotInGraveyard(player1, "Cabal Therapy");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Cabal Therapy"));
    }

    private void castFromHand(List<Card> targetHand) {
        harness.setHand(player1, List.of(new CabalTherapy()));
        harness.setHand(player2, targetHand);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, card);
        creature.setSummoningSick(false);
        return creature;
    }
}
