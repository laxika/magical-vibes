package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Lifeforce.class, ScatheZombies.class, HillGiant.class, DarkRitual.class})
class LifeforceTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a target black spell")
    void countersBlackSpell() {
        harness.addToBattlefield(player1, new Lifeforce());

        ScatheZombies zombies = new ScatheZombies();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, zombies, "{2}{B}");
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, zombies.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Scathe Zombies");
        harness.assertNotOnBattlefield(player2, "Scathe Zombies");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot counter a non-black spell")
    void cannotTargetNonBlackSpell() {
        harness.addToBattlefield(player1, new Lifeforce());

        HillGiant giant = new HillGiant();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, giant, "{3}{R}");
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters a target black instant spell")
    void countersBlackInstantSpell() {
        harness.addToBattlefield(player1, new Lifeforce());

        DarkRitual ritual = new DarkRitual();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, ritual, "{B}");
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, ritual.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dark Ritual");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without two green mana")
    void cannotActivateWithoutTwoGreenMana() {
        harness.addToBattlefield(player1, new Lifeforce());

        ScatheZombies zombies = new ScatheZombies();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, zombies, "{2}{B}");
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, zombies.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
