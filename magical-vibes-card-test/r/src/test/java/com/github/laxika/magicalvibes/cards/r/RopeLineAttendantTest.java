package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RopeLineAttendant.class, GrizzlyBears.class, Shock.class})
class RopeLineAttendantTest extends BaseCardTest {

    @Test
    void creatureCardsInHandPerpetuallyCreateCitizensWhenTheyEnter() {
        GrizzlyBears creatureCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new RopeLineAttendant(), creatureCard, new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent citizen = findPermanent(player1, "Citizen");
        assertThat(citizen.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(citizen.getCard().getPower()).isEqualTo(1);
        assertThat(citizen.getCard().getToughness()).isEqualTo(1);
        assertThat(citizen.getCard().getSubtypes()).containsExactly(CardSubtype.CITIZEN);
    }

    @Test
    @DisplayName("The grant to cards in hand is one log line that names and counts no card")
    void handGrantIsOneLineWithoutCount() {
        harness.setHand(player1, List.of(new RopeLineAttendant(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.gameLog).filteredOn(line -> line.plainText().contains("perpetually"))
                .extracting(line -> line.plainText())
                .containsExactly("Each affected card in " + player1.getUsername()
                        + "'s hand perpetually gains an ability.");
    }

    @Test
    @DisplayName("The grant to cards in hand is logged also when no card in hand matches")
    void handGrantIsLoggedWithoutMatchingCard() {
        harness.setHand(player1, List.of(new RopeLineAttendant(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.gameLog).filteredOn(line -> line.plainText().contains("perpetually"))
                .extracting(line -> line.plainText())
                .containsExactly("Each affected card in " + player1.getUsername()
                        + "'s hand perpetually gains an ability.");
    }
}
