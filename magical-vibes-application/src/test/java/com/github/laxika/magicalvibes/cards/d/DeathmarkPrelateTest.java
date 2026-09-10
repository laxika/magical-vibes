package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathmarkPrelate.class, ScatheZombies.class, GrizzlyBears.class})
class DeathmarkPrelateTest extends BaseCardTest {

    @Test
    @DisplayName("Taps, sacrifices a Zombie, and destroys the target non-Zombie creature")
    void sacrificesZombieAndDestroysNonZombieCreature() {
        Permanent prelate = addReadyPrelate(player1);
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new ScatheZombies());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        addAbilityMana(player1);
        harness.activateAbility(player1, battlefieldIndex(player1, prelate), null, targetId);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(zombie);
        harness.passBothPriorities();

        assertThat(prelate.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Scathe Zombies");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a Zombie creature")
    void cannotTargetZombieCreature() {
        Permanent prelate = addReadyPrelate(player1);
        harness.addToBattlefield(player1, new ScatheZombies());
        harness.addToBattlefield(player2, new ScatheZombies());
        UUID targetId = harness.getPermanentId(player2, "Scathe Zombies");

        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, prelate), null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a Zombie to sacrifice")
    void cannotActivateWithoutZombie() {
        Permanent prelate = addReadyPrelate(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, prelate), null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate only as a sorcery")
    void cannotActivateOutsideMainPhase() {
        Permanent prelate = addReadyPrelate(player1);
        harness.addToBattlefield(player1, new ScatheZombies());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, prelate), null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyPrelate(Player player) {
        Permanent prelate = harness.addToBattlefieldAndReturn(player, new DeathmarkPrelate());
        prelate.setSummoningSick(false);
        return prelate;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void addAbilityMana(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.BLACK, 1);
    }
}
