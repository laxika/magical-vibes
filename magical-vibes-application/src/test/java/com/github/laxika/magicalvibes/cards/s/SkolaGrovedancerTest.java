package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FieldOfRuin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FuneralRites;
import com.github.laxika.magicalvibes.cards.o.OxOfAgonas;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkolaGrovedancer.class, Forest.class, GrizzlyBears.class,
        FuneralRites.class, OxOfAgonas.class, FieldOfRuin.class})
class SkolaGrovedancerTest extends BaseCardTest {

    @Test
    @DisplayName("Milling a land gains 1 life")
    void millingLandGainsLife() {
        harness.addToBattlefield(player1, new SkolaGrovedancer());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.assertLife(player1, 21);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Milling a nonland card does not gain life")
    void millingNonlandDoesNotGainLife() {
        harness.addToBattlefield(player1, new SkolaGrovedancer());
        harness.setLife(player1, 20);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The activated ability mills exactly the top card")
    void millsExactlyOneCard() {
        harness.addToBattlefield(player1, new SkolaGrovedancer());
        Forest top = new Forest();
        Forest next = new Forest();
        harness.setLibrary(player1, List.of(top, next));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("An empty library does not cause life gain or a loss from milling")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new SkolaGrovedancer());
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    @DisplayName("A tapped summoning-sick Grovedancer can activate repeatedly")
    void activatesRepeatedlyWithoutTapping() {
        harness.addToBattlefield(player1, new SkolaGrovedancer());
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        gd.playerBattlefields.get(player1.getId()).getFirst().setSummoningSick(true);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each Grovedancer triggers for the controller's milled land")
    void multipleGrovedancersEachGainLife() {
        harness.addToBattlefield(player1, new SkolaGrovedancer());
        harness.addToBattlefield(player1, new SkolaGrovedancer());
        harness.addToBattlefield(player2, new SkolaGrovedancer());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Two lands milled together each trigger life gain")
    void batchMillingTriggersForEachLand() {
        harness.addToBattlefield(player1, new SkolaGrovedancer());
        harness.setHand(player1, List.of(new FuneralRites()));
        harness.setLibrary(player1, List.of(new SkolaGrovedancer(), new SkolaGrovedancer(),
                new Forest(), new Forest()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card instanceof Forest)).hasSize(2);
    }

    @Test
    @DisplayName("An opponent milling lands does not trigger Grovedancer")
    void opponentMillingDoesNotGainLife() {
        harness.addToBattlefield(player1, new SkolaGrovedancer());
        harness.setHand(player2, List.of(new FuneralRites()));
        harness.setLibrary(player2, List.of(new SkolaGrovedancer(), new SkolaGrovedancer(),
                new Forest(), new Forest()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castSorcery(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gd.playerGraveyards.get(player2.getId()).stream()
                .filter(card -> card instanceof Forest)).hasSize(2);
    }

    @Test
    @DisplayName("Discarded lands each trigger Grovedancer")
    void discardingLandsGainsLife() {
        harness.addToBattlefield(player1, new SkolaGrovedancer());
        harness.setHand(player1, List.of(new OxOfAgonas(), new Forest(), new Forest(),
                new SkolaGrovedancer()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card instanceof Forest)).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Sacrificing a land triggers for its owner, destroying an opposing land does not")
    void landGoingToGraveyardFromBattlefieldGainsLife() {
        harness.addToBattlefield(player1, new SkolaGrovedancer());
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new FieldOfRuin());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 1, null,
                harness.getPermanentId(player2, "Field of Ruin"));
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Field of Ruin");
        harness.assertInGraveyard(player2, "Field of Ruin");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
