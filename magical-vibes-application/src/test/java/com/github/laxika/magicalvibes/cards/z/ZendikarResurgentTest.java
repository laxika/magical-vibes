package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.CanopyGorger;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZendikarResurgent.class, Forest.class, GrizzlyBears.class, Shock.class,
        CanopyGorger.class, Wastes.class})
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

    @Test
    @DisplayName("Colorless mana is doubled immediately without using the stack")
    void addsColorlessManaImmediately() {
        harness.addToBattlefield(player1, new ZendikarResurgent());
        harness.addToBattlefield(player1, new Wastes());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Each copy adds one extra mana independently")
    void multipleCopiesEachAddMana() {
        harness.addToBattlefield(player1, new ZendikarResurgent());
        harness.addToBattlefield(player1, new ZendikarResurgent());
        harness.addToBattlefield(player1, new Wastes());

        harness.tapPermanent(player1, 2);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The draw resolves before the creature spell")
    void drawsBeforeCreatureResolves() {
        Wastes drawn = new Wastes();
        harness.addToBattlefield(player1, new ZendikarResurgent());
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new CanopyGorger()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Canopy Gorger");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Canopy Gorger");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Putting a creature onto the battlefield without casting does not draw")
    void doesNotDrawForCreatureEnteringWithoutBeingCast() {
        Wastes drawn = new Wastes();
        harness.addToBattlefield(player1, new ZendikarResurgent());
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new CanopyGorger());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }
}
