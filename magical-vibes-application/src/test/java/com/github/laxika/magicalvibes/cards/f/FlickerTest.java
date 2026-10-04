package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.g.GoliathBeetle;
import com.github.laxika.magicalvibes.cards.m.MaskOfLawAndGrace;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Flicker.class, GoliathBeetle.class, BraidwoodCup.class, MaskOfLawAndGrace.class, Card.class})
class FlickerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles and immediately returns any nontoken permanent under its owner's control")
    void flickersTargetPermanent() {
        harness.addToBattlefield(player2, new GoliathBeetle());
        harness.setHand(player1, List.of(new Flicker()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID beetleId = harness.getPermanentId(player2, "Goliath Beetle");

        harness.castAndResolveSorcery(player1, 0, beetleId);

        harness.assertOnBattlefield(player2, "Goliath Beetle");
        harness.assertNotOnBattlefield(player1, "Goliath Beetle");
        assertThat(harness.getPermanentId(player2, "Goliath Beetle")).isNotEqualTo(beetleId);
        Permanent returned = findPermanent(player2, "Goliath Beetle");
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Exiles and immediately returns a nontoken noncreature permanent")
    void flickersTargetArtifact() {
        Permanent cup = harness.addToBattlefieldAndReturn(player1, new BraidwoodCup());
        harness.setHand(player1, List.of(new Flicker()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, cup.getId());

        harness.assertOnBattlefield(player1, "Braidwood Cup");
        assertThat(harness.getPermanentId(player1, "Braidwood Cup")).isNotEqualTo(cup.getId());
    }

    @Test
    @DisplayName("Returns a stolen permanent under its owner's control")
    void returnsStolenPermanentToOwner() {
        harness.addToBattlefield(player2, new GoliathBeetle());
        UUID beetleId = harness.getPermanentId(player2, "Goliath Beetle");
        gd.stolenCreatures.put(beetleId, player1.getId());
        harness.setHand(player1, List.of(new Flicker()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, beetleId);

        harness.assertOnBattlefield(player1, "Goliath Beetle");
        harness.assertNotOnBattlefield(player2, "Goliath Beetle");
    }

    @Test
    @DisplayName("Cannot target a token permanent")
    void cannotTargetTokenPermanent() {
        Permanent token = harness.addToBattlefieldAndReturn(player1, token("Soldier Token"));
        harness.setHand(player1, List.of(new Flicker()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, token.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new GoliathBeetle());
        harness.setHand(player1, List.of(new Flicker()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, beetle.getId());

        gd.playerBattlefields.get(player1.getId()).remove(beetle);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Goliath Beetle");
    }

    @Test
    @DisplayName("A returning Aura's owner chooses what it enchants")
    void returningAuraOwnerChoosesAttachment() {
        Permanent originalCreature = harness.addToBattlefieldAndReturn(player2, new GoliathBeetle());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GoliathBeetle());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new MaskOfLawAndGrace());
        aura.setAttachedTo(originalCreature.getId());
        harness.setHand(player1, List.of(new Flicker()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, aura.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player2, otherCreature.getId());

        Permanent returned = findPermanent(player2, "Mask of Law and Grace");
        assertThat(returned.getId()).isNotEqualTo(aura.getId());
        assertThat(returned.getAttachedTo()).isEqualTo(otherCreature.getId());
        harness.assertNotInGraveyard(player2, "Mask of Law and Grace");
    }

    @Test
    @DisplayName("A flickered creature returns untapped and without its old counters")
    void returnsWithoutOldPermanentState() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new GoliathBeetle());
        beetle.tap();
        beetle.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new Flicker()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, beetle.getId());

        Permanent returned = findPermanent(player1, "Goliath Beetle");
        assertThat(returned.getId()).isNotEqualTo(beetle.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private static Card token(String name) {
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
