package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Squidnapper.class, RuneclawBear.class, DoomBlade.class, TurnToFrog.class})
class SquidnapperTest extends BaseCardTest {

    @Test
    @DisplayName("gains control of an opponent's creature until it leaves")
    void gainsControlUntilSourceLeaves() {
        Permanent bear = castSquidnapper();

        UUID squidnapperId = harness.getPermanentId(player1, "Squidnapper");
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, squidnapperId);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("the targeted creature's owner can pay the ransom to regain control")
    void ownerCanPayRansom() {
        Permanent bear = castSquidnapper();
        int startingLife = gd.getLife(player2.getId());

        harness.addMana(player2, ManaColor.COLORLESS, 6);
        harness.activateAbility(player2, indexOf(player1, bear), 0, null, null);

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
    }

    @Test
    @DisplayName("the creature's new controller cannot pay its ransom")
    void thiefCannotPayRansom() {
        Permanent bear = castSquidnapper();

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, bear), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("owner");
    }

    @Test
    @DisplayName("cannot target a creature controlled by its caster")
    void cannotTargetOwnCreature() {
        Permanent ownBear = addCreatureReady(player1, new RuneclawBear());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, ownBear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("no control change if Squidnapper leaves before its trigger resolves")
    void sourceLeavesBeforeTriggerResolves() {
        Permanent bear = addCreatureReady(player2, new RuneclawBear());
        prepareCast();
        harness.castCreature(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Squidnapper"));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
    }

    @Test
    @DisplayName("ransom cannot be paid while Squidnapper has lost its abilities")
    void cannotPayRansomWhenSquidnapperLosesAbilities() {
        Permanent bear = castSquidnapper();
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Squidnapper"));
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        int startingLife = gd.getLife(player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player2, indexOf(player1, bear), 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
    }

    @Test
    @DisplayName("the stolen creature losing abilities does not remove Squidnapper's ransom")
    void canPayRansomWhenStolenCreatureLosesAbilities() {
        Permanent bear = castSquidnapper();
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        int startingLife = gd.getLife(player2.getId());

        harness.activateAbility(player2, indexOf(player1, bear), 0, null, null);

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ransom requires enough life to pay the entire cost")
    void cannotPayRansomWithInsufficientLife() {
        Permanent bear = castSquidnapper();
        harness.setLife(player2, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player2, indexOf(player1, bear), 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getLife(player2.getId())).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
    }

    private Permanent castSquidnapper() {
        Permanent bear = addCreatureReady(player2, new RuneclawBear());
        prepareCast();
        harness.castCreature(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return bear;
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new Squidnapper()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

}
