package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FlameSweep;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CeruleanDrake.class, Shock.class, GreenwoodSentinel.class, ChandrasOutrage.class, FlameSweep.class})
class CeruleanDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices to counter a spell that targets its controller")
    void countersSpellTargetingController() {
        harness.addToBattlefield(player1, new CeruleanDrake());

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, shock.getId());
        harness.assertNotOnBattlefield(player1, "Cerulean Drake");
        harness.assertInGraveyard(player1, "Cerulean Drake");
        assertThat(harness.getGameData().stack).hasSize(2);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Cerulean Drake");
        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a spell that targets a creature")
    void cannotTargetSpellTargetingCreature() {
        harness.addToBattlefield(player1, new CeruleanDrake());
        harness.addToBattlefield(player1, new GreenwoodSentinel());

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Greenwood Sentinel"));
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-targeting spell")
    void cannotTargetNonTargetingSpell() {
        harness.addToBattlefield(player1, new CeruleanDrake());

        GreenwoodSentinel sentinel = new GreenwoodSentinel();
        harness.setHand(player2, List.of(sentinel));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, sentinel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be targeted by a red spell")
    void hasProtectionFromRed() {
        harness.addToBattlefield(player1, new CeruleanDrake());

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(
                player2, 0, harness.getPermanentId(player1, "Cerulean Drake")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot counter a spell targeting the opponent")
    void cannotCounterSpellTargetingOpponent() {
        harness.addToBattlefield(player1, new CeruleanDrake());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player2.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Cerulean Drake");
        harness.assertNotInGraveyard(player1, "Cerulean Drake");
    }

    @Test
    @DisplayName("Can counter its controller's own spell targeting them")
    void countersOwnSpellTargetingController() {
        harness.addToBattlefield(player1, new CeruleanDrake());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cerulean Drake");
        harness.assertInGraveyard(player1, "Shock");
        harness.assertLife(player1, 20);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Damage to a creature's controller does not target that player")
    void cannotCounterSpellThatOnlyIndirectlyDamagesController() {
        harness.addToBattlefield(player1, new CeruleanDrake());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        ChandrasOutrage outrage = new ChandrasOutrage();
        harness.setHand(player2, List.of(outrage));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Greenwood Sentinel"));
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, outrage.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Cerulean Drake");
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Protection prevents damage from an opponent's non-targeting red spell")
    void survivesNonTargetingRedDamage() {
        harness.addToBattlefield(player1, new CeruleanDrake());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setHand(player2, List.of(new FlameSweep()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0);

        harness.assertOnBattlefield(player1, "Cerulean Drake");
        harness.assertNotInGraveyard(player1, "Cerulean Drake");
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        harness.assertInGraveyard(player2, "Flame Sweep");
    }
}
