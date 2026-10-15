package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EternalIsolation.class, AirElemental.class, CentaurCourser.class, Disfigure.class})
class EternalIsolationTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target creature with power 4 or greater on the bottom of its owner's library")
    void tucksHighPowerCreature() {
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new EternalIsolation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertNotInGraveyard(player2, "Air Elemental");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getLast().getName()).isEqualTo("Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetLowPowerCreature() {
        harness.addToBattlefield(player2, new CentaurCourser());
        UUID targetId = harness.getPermanentId(player2, "Centaur Courser");

        harness.setHand(player1, List.of(new EternalIsolation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Power falling below four before resolution makes the target illegal")
    void targetLosingPowerIsNotTucked() {
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new EternalIsolation()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, targetId);

        harness.setHand(player2, List.of(new Disfigure()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        harness.assertInGraveyard(player1, "Eternal Isolation");
    }

    @Test
    @DisplayName("Can target a creature controlled by the caster")
    void canTuckOwnCreature() {
        harness.addToBattlefield(player1, new AirElemental());
        UUID targetId = harness.getPermanentId(player1, "Air Elemental");
        harness.setHand(player1, List.of(new EternalIsolation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Air Elemental");
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getName()).isEqualTo("Air Elemental");
    }

    @Test
    @DisplayName("A creature controlled by another player goes to its owner's library")
    void tucksStolenCreatureIntoOwnersLibrary() {
        AirElemental creature = new AirElemental();
        creature.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, creature);
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        gd.stolenCreatures.put(targetId, player1.getId());
        int controllerDeckSize = gd.playerDecks.get(player2.getId()).size();
        int ownerDeckSize = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new EternalIsolation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(controllerDeckSize);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(ownerDeckSize + 1);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(creature);
    }

    @Test
    @DisplayName("Uses current power rather than printed power when choosing a target")
    void canTuckCreatureWithIncreasedPower() {
        harness.addToBattlefieldAndReturn(player2, new CentaurCourser()).setPowerModifier(1);
        UUID targetId = harness.getPermanentId(player2, "Centaur Courser");
        harness.setHand(player1, List.of(new EternalIsolation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Centaur Courser");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getName()).isEqualTo("Centaur Courser");
    }
}
