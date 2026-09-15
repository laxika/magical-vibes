package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.n.NantukoMonastery;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiftstonePortal.class, NantukoMonastery.class})
class RiftstonePortalTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one colorless mana")
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new RiftstonePortal());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("In the graveyard, grants your lands a green or white mana ability")
    void grantsManaAbilityFromGraveyard() {
        harness.setGraveyard(player1, List.of(new RiftstonePortal()));
        harness.addToBattlefield(player1, new NantukoMonastery());
        harness.addToBattlefield(player2, new NantukoMonastery());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The graveyard ability can add white mana")
    void grantsWhiteManaFromGraveyard() {
        harness.setGraveyard(player1, List.of(new RiftstonePortal()));
        harness.addToBattlefield(player1, new NantukoMonastery());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Grants the mana ability to every land you control")
    void grantsManaAbilityToEveryOwnLand() {
        harness.setGraveyard(player1, List.of(new RiftstonePortal()));
        harness.addToBattlefield(player1, new NantukoMonastery());
        harness.addToBattlefield(player1, new NantukoMonastery());

        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not grant the graveyard ability while on the battlefield")
    void doesNotGrantManaAbilityFromBattlefield() {
        harness.addToBattlefield(player1, new RiftstonePortal());
        harness.addToBattlefield(player1, new NantukoMonastery());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Stops granting the mana ability when it leaves the graveyard")
    void stopsGrantingManaAbilityWhenItLeavesGraveyard() {
        harness.setGraveyard(player1, List.of(new RiftstonePortal()));
        harness.addToBattlefield(player1, new NantukoMonastery());

        harness.setGraveyard(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
