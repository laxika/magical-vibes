package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeepOut.class, GrizzlyBears.class, AngelicChorus.class})
class KeepOutTest extends BaseCardTest {

    @Test
    @DisplayName("Damage mode deals 4 damage to a tapped creature")
    void damageModeDealsDamageToTappedCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();

        harness.setHand(player1, List.of(new KeepOut()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroy mode destroys a target enchantment")
    void destroyModeDestroysEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());

        harness.setHand(player1, List.of(new KeepOut()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Angelic Chorus"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Damage mode cannot target an untapped creature")
    void damageModeCannotTargetUntappedCreature() {
        Permanent untappedBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent tappedBears = addCreatureReady(player2, new GrizzlyBears());
        tappedBears.tap();

        harness.setHand(player1, List.of(new KeepOut()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, untappedBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroy mode cannot target a creature")
    void destroyModeCannotTargetCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new KeepOut()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage mode does not resolve if its target becomes untapped")
    void damageModeDoesNotDamageCreatureThatUntaps() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();
        harness.setHand(player1, List.of(new KeepOut()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 0, bears.getId());
        bears.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Keep Out");
    }

    @Test
    @DisplayName("Damage mode deals exactly four damage and can target your own creature")
    void damageModeDealsExactlyFourDamageToOwnCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        bears.tap();
        harness.setHand(player1, List.of(new KeepOut()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Keep Out");
    }

    @Test
    @DisplayName("Destroy mode can destroy your own enchantment without a tapped creature")
    void destroyModeCanDestroyOwnEnchantment() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.setHand(player1, List.of(new KeepOut()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 1, harness.getPermanentId(player1, "Angelic Chorus"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
        harness.assertInGraveyard(player1, "Angelic Chorus");
        harness.assertInGraveyard(player1, "Keep Out");
    }
}
