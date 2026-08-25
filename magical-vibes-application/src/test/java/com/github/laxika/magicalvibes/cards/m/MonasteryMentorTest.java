package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MonasteryMentor.class, GrizzlyBears.class, Shock.class})
class MonasteryMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell boosts Monastery Mentor and creates a Monk token")
    void noncreatureSpellBoostsAndCreatesMonk() {
        harness.addToBattlefield(player1, new MonasteryMentor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mentor = findPermanent(player1, "Monastery Mentor");
        Permanent token = findPermanent(player1, "Monk");
        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(3);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.MONK);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Monk tokens get prowess when their controller casts a noncreature spell")
    void monkTokensHaveProwess() {
        harness.addToBattlefield(player1, new MonasteryMentor());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Monk");

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Monastery Mentor")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new MonasteryMentor());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        Permanent mentor = findPermanent(player1, "Monastery Mentor");
        assertThat(countPermanents(player1, "Monk")).isZero();
        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(2);
    }
}
