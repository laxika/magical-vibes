package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoseNoble.class, Shock.class})
class RoseNobleTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Doctor spell draws a card")
    void doctorSpellDrawsCard() {
        addRose();
        castAndResolve(doctorSpell());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Casting a creature with doctor's companion draws a card")
    void doctorsCompanionCreatureDrawsCard() {
        addRose();
        castAndResolve(creatureWithDoctorsCompanion());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A nonmatching creature spell does not draw a card")
    void unrelatedCreatureDoesNotDrawCard() {
        addRose();
        castAndResolve(creature(CardSubtype.HUMAN));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A noncreature spell with doctor's companion does not draw a card")
    void noncreatureDoctorsCompanionDoesNotDrawCard() {
        addRose();
        Card spell = card("Noncreature companion", CardType.INSTANT, Set.of(Keyword.DOCTORS_COMPANION));
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(new RoseNoble()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ward {2} counters an opponent's spell when they cannot pay")
    void wardCountersUnpaidSpell() {
        Permanent rose = harness.addToBattlefieldAndReturn(player1, new RoseNoble());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, rose.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }

    private void addRose() {
        harness.addToBattlefield(player1, new RoseNoble());
        harness.setLibrary(player1, List.of(new RoseNoble()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castAndResolve(Card spell) {
        harness.setHand(player1, List.of(spell));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Card doctorSpell() {
        return creature(CardSubtype.DOCTOR);
    }

    private Card creatureWithDoctorsCompanion() {
        return card("Companion creature", CardType.CREATURE, Set.of(Keyword.DOCTORS_COMPANION), CardSubtype.HUMAN);
    }

    private Card creature(CardSubtype subtype) {
        return card("Creature", CardType.CREATURE, Set.of(), subtype);
    }

    private Card card(String name, CardType type, Set<Keyword> keywords, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost("{1}");
        card.setColor(CardColor.BLUE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(subtypes));
        card.setKeywords(keywords);
        return card;
    }
}
