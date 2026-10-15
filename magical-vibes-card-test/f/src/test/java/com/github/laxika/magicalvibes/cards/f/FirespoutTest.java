package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DevotedDruid;
import com.github.laxika.magicalvibes.cards.b.BriarberryCohort;
import com.github.laxika.magicalvibes.cards.p.Plumeveil;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Firespout.class, DevotedDruid.class, BriarberryCohort.class, Plumeveil.class})
class FirespoutTest extends BaseCardTest {

    @Test
    @DisplayName("Only {R} spent: kills non-flyers, spares flyers")
    void redOnlyKillsNonFlyers() {
        harness.addToBattlefield(player2, new DevotedDruid());
        harness.addToBattlefield(player2, new BriarberryCohort());

        harness.setHand(player1, List.of(new Firespout()));
        // {2} generic + {R/G} hybrid all paid with red → only {R} spent
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player2, "Devoted Druid");
        harness.assertOnBattlefield(player2, "Briarberry Cohort");
    }

    @Test
    @DisplayName("Only {G} spent: kills flyers, spares non-flyers")
    void greenOnlyKillsFlyers() {
        harness.addToBattlefield(player2, new DevotedDruid());
        harness.addToBattlefield(player2, new BriarberryCohort());

        harness.setHand(player1, List.of(new Firespout()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Devoted Druid");
        harness.assertNotOnBattlefield(player2, "Briarberry Cohort");
    }

    @Test
    @DisplayName("{R}{G} spent: kills both flyers and non-flyers")
    void bothColorsKillEverything() {
        harness.addToBattlefield(player2, new DevotedDruid());
        harness.addToBattlefield(player2, new BriarberryCohort());

        harness.setHand(player1, List.of(new Firespout()));
        // Pay with both colors so both {R} and {G} count as spent (generic {2} draws the
        // off-color the hybrid didn't take).
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player2, "Devoted Druid");
        harness.assertNotOnBattlefield(player2, "Briarberry Cohort");
    }
    @Test
    @DisplayName("Mixed payment damages creatures on both sides but does not damage players")
    void damagesBothControllersCreatures() {
        for (var player : List.of(player1, player2)) {
            harness.addToBattlefield(player, new DevotedDruid());
            harness.addToBattlefield(player, new BriarberryCohort());
            harness.setLife(player, 20);
        }
        harness.setHand(player1, List.of(new Firespout()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        for (var player : List.of(player1, player2)) {
            harness.assertInGraveyard(player, "Devoted Druid");
            harness.assertInGraveyard(player, "Briarberry Cohort");
            harness.assertLife(player, 20);
        }
        harness.assertInGraveyard(player1, "Firespout");
    }

    @Test
    @DisplayName("Spending three green mana deals exactly three damage to a surviving flyer")
    void damageDoesNotScaleWithManaSpent() {
        harness.addToBattlefield(player2, new Plumeveil());
        harness.setHand(player1, List.of(new Firespout()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Plumeveil");
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Green mana added after a red-only cast does not enable damage to flyers")
    void unspentManaDoesNotEnableOtherColor() {
        harness.addToBattlefield(player2, new DevotedDruid());
        harness.addToBattlefield(player2, new BriarberryCohort());
        harness.setHand(player1, List.of(new Firespout()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, 0);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Devoted Druid");
        harness.assertOnBattlefield(player2, "Briarberry Cohort");
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getMarkedDamage()).isZero();
    }
}
