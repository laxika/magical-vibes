package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteelbaneHydra.class, AngelsFeather.class, AngelicChorus.class, GrizzlyBears.class})
class SteelbaneHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X +1/+1 counters")
    void entersWithXPlusOnePlusOneCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SteelbaneHydra()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent hydra = findHydra(player1);
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(hydra.getEffectivePower()).isEqualTo(2);
        assertThat(hydra.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes a +1/+1 counter to destroy an artifact")
    void destroysArtifact() {
        Permanent hydra = addReadyHydra(player1);
        harness.addToBattlefield(player2, new AngelsFeather());
        activateAgainst(player2, "Angel's Feather");

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player2, "Angel's Feather");
        harness.assertInGraveyard(player2, "Angel's Feather");
    }

    @Test
    @DisplayName("Removes a +1/+1 counter to destroy an enchantment")
    void destroysEnchantment() {
        addReadyHydra(player1);
        harness.addToBattlefield(player2, new AngelicChorus());
        activateAgainst(player2, "Angelic Chorus");

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadyHydra(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    private void activateAgainst(Player targetPlayer, String targetName) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(targetPlayer, targetName);
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
    }

    private Permanent addReadyHydra(Player player) {
        Permanent hydra = new Permanent(new SteelbaneHydra());
        hydra.setSummoningSick(false);
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerBattlefields.get(player.getId()).add(hydra);
        return hydra;
    }

    private Permanent findHydra(Player player) {
        return findPermanent(player, "Steelbane Hydra");
    }
}
