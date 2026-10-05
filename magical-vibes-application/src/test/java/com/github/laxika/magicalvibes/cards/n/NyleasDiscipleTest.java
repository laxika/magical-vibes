package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BowOfNylea;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.v.VoyagesEnd;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Nylea's Disciple")
@CardUsed({NyleasDisciple.class, GrizzlyBears.class, SuntailHawk.class, VoyagesEnd.class, BowOfNylea.class})
class NyleasDiscipleTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains life equal to green devotion, including itself")
    void etbGainsLifeEqualToGreenDevotion() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.setHand(player1, List.of(new NyleasDisciple()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapped noncreature permanents contribute only their mana-cost symbols")
    void countsTappedNoncreatureManaCostOnce() {
        harness.addToBattlefieldAndReturn(player1, new BowOfNylea()).setTapped(true);
        harness.setHand(player1, List.of(new NyleasDisciple()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Devotion excludes opposing permanents and cards outside the battlefield")
    void excludesOpposingPermanentsAndOtherZones() {
        harness.addToBattlefield(player2, new NyleasDisciple());
        harness.setGraveyard(player1, List.of(new NyleasDisciple()));
        harness.setExile(player1, List.of(new NyleasDisciple()));
        harness.setLibrary(player1, List.of(new NyleasDisciple()));
        harness.setHand(player1, List.of(new NyleasDisciple(), new NyleasDisciple()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing the source before resolution can reduce devotion to zero")
    void removedSourceGainsNoLifeWithZeroDevotion() {
        bounceDiscipleBeforeTriggerResolves();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A removed source still gains life from remaining green permanents")
    void removedSourceStillGainsLifeFromRemainingDevotion() {
        harness.addToBattlefield(player1, new NyleasDisciple());

        bounceDiscipleBeforeTriggerResolves();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    private void bounceDiscipleBeforeTriggerResolves() {
        NyleasDisciple disciple = new NyleasDisciple();
        harness.setHand(player1, List.of(disciple));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        var source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(disciple.getId()))
                .findFirst().orElseThrow();
        harness.setHand(player2, List.of(new VoyagesEnd()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, source.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Nylea's Disciple");
    }
}
