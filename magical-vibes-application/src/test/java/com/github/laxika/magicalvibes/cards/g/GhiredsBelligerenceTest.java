package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhiredsBelligerence.class, GrizzlyBears.class, ColossalDreadmaw.class, Shock.class})
class GhiredsBelligerenceTest extends BaseCardTest {

    @Test
    @DisplayName("Deals divided damage and populates when any damaged creature dies")
    void dealsDamageAndPopulatesForAnyDamagedCreatureDeath() {
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castBelligerence(Map.of(bears.getId(), 2));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1,
                gd.playerBattlefields.get(player1.getId()).stream()
                        .filter(p -> p.getCard().getName().equals("Soldier Token"))
                        .findFirst().orElseThrow().getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p -> p.getCard().isToken()).hasSize(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Populates when a damaged creature dies later in the turn")
    void populatesForLaterDeathThisTurn() {
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castBelligerence(1, Map.of(bears.getId(), 1));

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1,
                gd.playerBattlefields.get(player1.getId()).stream()
                        .filter(p -> p.getCard().getName().equals("Soldier Token"))
                        .findFirst().orElseThrow().getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p -> p.getCard().isToken()).hasSize(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger for an undamaged creature")
    void doesNotTriggerForUndamagedCreature() {
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent dreadmaw = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        castBelligerence(Map.of(dreadmaw.getId(), 2));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p -> p.getCard().isToken()).hasSize(1);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void castBelligerence(Map<java.util.UUID, Integer> assignments) {
        castBelligerence(2, assignments);
    }

    private void castBelligerence(int xValue, Map<java.util.UUID, Integer> assignments) {
        harness.setHand(player1, List.of(new GhiredsBelligerence()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castSorceryForX(player1, 0, xValue, assignments);
        harness.passBothPriorities();
    }

    private static Card creatureToken(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
