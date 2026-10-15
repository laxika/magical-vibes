package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EndBlazeEpiphany;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TanufelRimespeaker.class, JacesIngenuity.class, GrizzlyBears.class, EndBlazeEpiphany.class})
class TanufelRimespeakerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell with mana value 4 or greater draws a card")
    void highManaValueSpellDrawsCard() {
        harness.addToBattlefield(player1, new TanufelRimespeaker());
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Casting a spell with mana value less than 4 does not draw a card")
    void lowManaValueSpellDoesNotDrawCard() {
        harness.addToBattlefield(player1, new TanufelRimespeaker());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A creature spell with mana value exactly four triggers before it resolves")
    void exactlyFourManaValueCreatureDrawsBeforeResolving() {
        harness.addToBattlefield(player1, new TanufelRimespeaker());
        harness.setHand(player1, List.of(new TanufelRimespeaker()));
        harness.setLibrary(player1, List.of(new TanufelRimespeaker()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The chosen value of X counts toward the spell's mana value")
    void xSpellReachingFourManaValueDrawsCard() {
        harness.addToBattlefield(player1, new TanufelRimespeaker());
        harness.setHand(player1, List.of(new EndBlazeEpiphany()));
        harness.setLibrary(player1, List.of(new TanufelRimespeaker()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, 3, harness.getPermanentId(player1, "Tanufel Rimespeaker"));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An X spell with total mana value below four does not trigger")
    void xSpellBelowFourManaValueDoesNotDrawCard() {
        harness.addToBattlefield(player1, new TanufelRimespeaker());
        harness.setHand(player1, List.of(new EndBlazeEpiphany()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, 2, harness.getPermanentId(player1, "Tanufel Rimespeaker"));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's qualifying spell does not draw a card for the controller")
    void opponentsSpellDoesNotDrawCard() {
        harness.addToBattlefield(player1, new TanufelRimespeaker());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new JacesIngenuity()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Tanufel Rimespeaker does not trigger from its own cast")
    void ownCastDoesNotDrawCard() {
        harness.setHand(player1, List.of(new TanufelRimespeaker()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Tanufel Rimespeaker");
    }

    @Test
    @DisplayName("Each Rimespeaker draws a card for the same qualifying spell")
    void multipleRimespeakersEachDrawCard() {
        harness.addToBattlefield(player1, new TanufelRimespeaker());
        harness.addToBattlefield(player1, new TanufelRimespeaker());
        harness.setHand(player1, List.of(new TanufelRimespeaker()));
        harness.setLibrary(player1, List.of(new TanufelRimespeaker(), new TanufelRimespeaker()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);

        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Every qualifying spell in a turn triggers all Rimespeakers already on the battlefield")
    void successiveQualifyingSpellsEachTrigger() {
        harness.addToBattlefield(player1, new TanufelRimespeaker());
        harness.setHand(player1, List.of(new TanufelRimespeaker(), new TanufelRimespeaker()));
        harness.setLibrary(player1, List.of(
                new TanufelRimespeaker(), new TanufelRimespeaker(), new TanufelRimespeaker()));
        harness.addMana(player1, ManaColor.BLUE, 8);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }
}
