package com.github.laxika.magicalvibes.cards.w;

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

@CardUsed({WickerboughElder.class, AngelsFeather.class, AngelicChorus.class, GrizzlyBears.class})
class WickerboughElderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with a -1/-1 counter (4/4 becomes 3/3)")
    void entersWithMinusCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WickerboughElder()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent elder = findPermanent(player1, "Wickerbough Elder");
        assertThat(gd.stack).isEmpty();
        assertThat(elder.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(elder.getEffectivePower()).isEqualTo(3);
        assertThat(elder.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability removes a -1/-1 counter and destroys target artifact")
    void abilityDestroysArtifact() {
        Permanent elder = addReadyElder(player1);
        harness.addToBattlefield(player2, new AngelsFeather());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player2, "Angel's Feather");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(elder.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
        harness.assertNotOnBattlefield(player2, "Angel's Feather");
        harness.assertInGraveyard(player2, "Angel's Feather");
    }

    @Test
    @DisplayName("Ability destroys target enchantment")
    void abilityDestroysEnchantment() {
        addReadyElder(player1);
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Cannot activate ability when no -1/-1 counters remain")
    void cannotActivateWithoutCounters() {
        Permanent elder = addReadyElder(player1);
        elder.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        harness.addToBattlefield(player2, new AngelsFeather());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player2, "Angel's Feather");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadyElder(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    @Test
    @DisplayName("A summoning-sick Elder can pay its counter cost before resolution")
    void summoningSickElderPaysCounterImmediately() {
        Permanent elder = addReadyElder(player1);
        elder.setSummoningSick(true);
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Angel's Feather"));

        assertThat(elder.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(elder.getEffectivePower()).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Angel's Feather");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Angel's Feather");
    }

    private Permanent addReadyElder(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new WickerboughElder());
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        return perm;
    }

}
