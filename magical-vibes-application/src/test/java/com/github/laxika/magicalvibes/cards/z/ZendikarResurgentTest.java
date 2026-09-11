package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZendikarResurgent.class, Forest.class, GrizzlyBears.class, Shock.class})
class ZendikarResurgentTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a land you control adds one additional mana it produced")
    void addsManaWhenControllerTapsLand() {
        harness.addToBattlefield(player1, new ZendikarResurgent());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's land does not get the additional mana")
    void doesNotAddManaWhenOpponentTapsLand() {
        harness.addToBattlefield(player1, new ZendikarResurgent());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell triggers a draw")
    void drawsWhenControllerCastsCreature() {
        Forest drawn = new Forest();
        harness.addToBattlefield(player1, new ZendikarResurgent());
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Zendikar Resurgent"));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Casting a noncreature spell does not trigger a draw")
    void doesNotDrawWhenControllerCastsNoncreature() {
        harness.addToBattlefield(player1, new ZendikarResurgent());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Zendikar Resurgent"));
    }
}
