package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.cards.b.BartizanBats;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeafeningClarion.class, VernadiShieldmate.class, BartizanBats.class, DouserOfLights.class})
class DeafeningClarionTest extends BaseCardTest {

    @Test
    @DisplayName("The damage mode deals 3 damage to each creature")
    void dealsDamageToEachCreature() {
        harness.addToBattlefield(player1, new VernadiShieldmate());
        harness.addToBattlefield(player2, new VernadiShieldmate());
        harness.addToBattlefield(player2, new BartizanBats());
        cast(new int[]{0});

        harness.assertNotOnBattlefield(player1, "Vernadi Shieldmate");
        harness.assertNotOnBattlefield(player2, "Vernadi Shieldmate");
        harness.assertNotOnBattlefield(player2, "Bartizan Bats");
    }

    @Test
    @DisplayName("The lifelink mode grants lifelink to your creatures only until end of turn")
    void grantsLifelinkUntilEndOfTurn() {
        harness.addToBattlefield(player1, new VernadiShieldmate());
        harness.addToBattlefield(player2, new VernadiShieldmate());
        cast(new int[]{1});

        var ownCreature = findPermanent(player1, "Vernadi Shieldmate");
        var opponentCreature = findPermanent(player2, "Vernadi Shieldmate");
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.LIFELINK)).isFalse();

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Choosing both modes deals damage and grants lifelink")
    void choosesBothModes() {
        harness.addToBattlefield(player1, new DouserOfLights());
        harness.addToBattlefield(player2, new VernadiShieldmate());
        cast(new int[]{0, 1});

        var ownCreature = findPermanent(player1, "Douser of Lights");
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isTrue();
        harness.assertNotOnBattlefield(player2, "Vernadi Shieldmate");
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain lifelink")
    void doesNotGrantLifelinkToLaterCreatures() {
        harness.addToBattlefield(player1, new VernadiShieldmate());
        cast(new int[]{1});
        harness.addToBattlefield(player1, new DouserOfLights());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Vernadi Shieldmate"), Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Douser of Lights"), Keyword.LIFELINK)).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Damage mode leaves surviving creatures without lifelink and does not damage players")
    void damageModeDoesNotGrantLifelinkOrDamagePlayers() {
        harness.addToBattlefield(player1, new DouserOfLights());
        harness.addToBattlefield(player2, new DouserOfLights());
        cast(new int[]{0});

        harness.assertOnBattlefield(player1, "Douser of Lights");
        harness.assertOnBattlefield(player2, "Douser of Lights");
        assertThat(findPermanent(player1, "Douser of Lights").getMarkedDamage()).isEqualTo(3);
        assertThat(findPermanent(player2, "Douser of Lights").getMarkedDamage()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Douser of Lights"), Keyword.LIFELINK)).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Both modes resolve in printed order even when selected in reverse order")
    void bothModesDoNotGrantLifeForClarionsDamage() {
        harness.addToBattlefield(player1, new DouserOfLights());
        harness.addToBattlefield(player1, new VernadiShieldmate());
        harness.addToBattlefield(player2, new VernadiShieldmate());
        cast(new int[]{1, 0});

        harness.assertNotOnBattlefield(player1, "Vernadi Shieldmate");
        harness.assertNotOnBattlefield(player2, "Vernadi Shieldmate");
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Douser of Lights"), Keyword.LIFELINK)).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void cast(int[] modes) {
        harness.setHand(player1, List.of(new DeafeningClarion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, List.of(), null);
        harness.passBothPriorities();
    }
}
