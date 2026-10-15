package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrazcaRelic.class, Forest.class, GrizzlyBears.class})
class OrazcaRelicTest extends BaseCardTest {

    @Test
    @DisplayName("Ascend grants the city's blessing when Orazca Relic enters as the tenth permanent")
    void ascendGrantsBlessing() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new OrazcaRelic()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }

    @Test
    @DisplayName("Tapping Orazca Relic adds one colorless mana")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new OrazcaRelic());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot sacrifice Orazca Relic without the city's blessing")
    void cannotSacrificeWithoutBlessing() {
        harness.addToBattlefield(player1, new OrazcaRelic());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("city's blessing");
        harness.assertOnBattlefield(player1, "Orazca Relic");
    }

    @Test
    @DisplayName("With the city's blessing, sacrificing Orazca Relic gains life and draws a card")
    void sacrificeGainsLifeAndDrawsCard() {
        gd.playersWithCityBlessing.add(player1.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new OrazcaRelic());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Orazca Relic");
        harness.assertInGraveyard(player1, "Orazca Relic");
        harness.assertLife(player1, lifeBefore + 3);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ascend grants the blessing when another permanent brings the total to ten")
    void ascendWhenAnotherPermanentEnters() {
        harness.addToBattlefield(player1, new OrazcaRelic());
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent's permanents do not count toward ascend")
    void ascendCountsOnlyControllersPermanents() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player2, new Forest());
        }

        harness.enterBattlefieldAndReturn(player1, new OrazcaRelic());

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId(), player2.getId());
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and the blessing persists below ten permanents")
    void sacrificeIsCostAndBlessingPersists() {
        harness.addToBattlefield(player1, new OrazcaRelic());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Orazca Relic");
        harness.assertInGraveyard(player1, "Orazca Relic");
        harness.assertLife(player1, 20);
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }

    @Test
    @DisplayName("A tapped Relic cannot pay the tap cost of its sacrifice ability")
    void cannotSacrificeTappedRelic() {
        gd.playersWithCityBlessing.add(player1.getId());
        harness.addToBattlefield(player1, new OrazcaRelic());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        harness.assertOnBattlefield(player1, "Orazca Relic");
        harness.assertNotInGraveyard(player1, "Orazca Relic");
        assertThat(gd.stack).isEmpty();
    }
}
