package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.o.OneWithTheStars;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnduringTenacity.class, AngelOfMercy.class, Murder.class,
        Disenchant.class, OneWithTheStars.class})
class EnduringTenacityTest extends BaseCardTest {

    @Test
    @DisplayName("Makes target opponent lose life equal to life gained")
    void opponentLosesLifeEqualToLifeGained() {
        harness.addToBattlefield(player1, new EnduringTenacity());

        int opponentLifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 3);
    }

    @Test
    @DisplayName("Returns from the graveyard as an enchantment when it dies as a creature")
    void returnsAsEnchantmentWhenItDiesAsCreature() {
        harness.addToBattlefield(player1, new EnduringTenacity());
        Permanent enduring = findPermanent(player1, "Enduring Tenacity");

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, enduring.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Enduring Tenacity");
        assertThat(gqs.getEffectiveCardTypes(gd, returned)).containsExactly(CardType.ENCHANTMENT);
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(gqs.isEnchantment(gd, returned)).isTrue();
    }

    @Test
    @DisplayName("Does not return when it dies as a noncreature")
    void doesNotReturnWhenItDiesAsNoncreature() {
        harness.addToBattlefield(player1, new EnduringTenacity());
        Permanent enduring = findPermanent(player1, "Enduring Tenacity");

        harness.setHand(player1, List.of(new OneWithTheStars()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, enduring.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, enduring)).isFalse();

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, enduring.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Enduring Tenacity");
        harness.assertNotOnBattlefield(player1, "Enduring Tenacity");
    }
}
