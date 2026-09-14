package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallerOfTheHunt.class, DeadlyInsect.class, FreshVolunteers.class})
class CallerOfTheHuntTest extends BaseCardTest {

    @Test
    void countsCreaturesOfTheChosenTypeAcrossTheBattlefield() {
        harness.addToBattlefield(player1, new DeadlyInsect());
        harness.addToBattlefield(player2, new DeadlyInsect());
        harness.addToBattlefield(player2, new FreshVolunteers());
        harness.setHand(player1, List.of(new CallerOfTheHunt()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreatureWithChosenType(player1, 0, CardSubtype.INSECT);
        harness.passBothPriorities();

        Permanent caller = findPermanent(player1, "Caller of the Hunt");
        assertThat(gqs.getEffectivePower(gd, caller)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, caller)).isEqualTo(2);

        harness.addToBattlefield(player2, new DeadlyInsect());
        assertThat(gqs.getEffectivePower(gd, caller)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, caller)).isEqualTo(3);
    }

    @Test
    void countsItselfWhenTheChosenTypeMatches() {
        harness.addToBattlefield(player2, new FreshVolunteers());
        harness.setHand(player1, List.of(new CallerOfTheHunt()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreatureWithChosenType(player1, 0, CardSubtype.HUMAN);
        harness.passBothPriorities();

        Permanent caller = findPermanent(player1, "Caller of the Hunt");
        assertThat(gqs.getEffectivePower(gd, caller)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, caller)).isEqualTo(2);

        harness.addToBattlefield(player1, new FreshVolunteers());
        assertThat(gqs.getEffectivePower(gd, caller)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, caller)).isEqualTo(3);
    }

    @Test
    void cannotBeCastWithoutChoosingAType() {
        harness.setHand(player1, List.of(new CallerOfTheHunt()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotBeCastWithANonCreatureType() {
        harness.setHand(player1, List.of(new CallerOfTheHunt()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreatureWithChosenType(player1, 0, CardSubtype.FOREST))
                .isInstanceOf(IllegalStateException.class);
    }
}
