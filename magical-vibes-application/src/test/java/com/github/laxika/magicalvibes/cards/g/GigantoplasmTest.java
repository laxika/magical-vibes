package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Gigantoplasm.class, GrizzlyBears.class})
class GigantoplasmTest extends BaseCardTest {

    @Test
    @DisplayName("Gigantoplasm copies a creature and can set its base power and toughness")
    void copiesCreatureAndSetsBasePowerToughness() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Gigantoplasm(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        Permanent gigantoplasm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Gigantoplasm"))
                .findFirst()
                .orElseThrow();
        assertThat(gigantoplasm.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gigantoplasm.getEffectivePower()).isEqualTo(2);
        assertThat(gigantoplasm.getEffectiveToughness()).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(gigantoplasm), 5, null);
        harness.passBothPriorities();

        assertThat(gigantoplasm.getEffectivePower()).isEqualTo(5);
        assertThat(gigantoplasm.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Gigantoplasm dies as a 0/0 when it does not copy a creature")
    void diesWhenItDoesNotCopy() {
        harness.castFromHand(player1, new Gigantoplasm(), "{3}{U}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gigantoplasm");
        harness.assertInGraveyard(player1, "Gigantoplasm");
    }
}
