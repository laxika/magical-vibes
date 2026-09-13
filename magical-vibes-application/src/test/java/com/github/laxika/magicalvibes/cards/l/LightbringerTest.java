package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.n.NetterEnDal;
import com.github.laxika.magicalvibes.cards.s.SealOfDoom;
import com.github.laxika.magicalvibes.cards.s.SpinelessThug;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Lightbringer.class, SpinelessThug.class, NetterEnDal.class, SealOfDoom.class})
class LightbringerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Lightbringer sacrifices it and exiles a black creature")
    void sacrificesSelfAndExilesBlackCreature() {
        addLightbringer(player1);
        Permanent target = addCreatureReady(player2, new SpinelessThug());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Lightbringer");
        harness.assertOnBattlefield(player2, "Spineless Thug");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spineless Thug");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Spineless Thug"));
    }

    @Test
    @DisplayName("Cannot target a non-black creature")
    void cannotTargetNonBlackCreature() {
        addLightbringer(player1);
        Permanent target = addCreatureReady(player2, new NetterEnDal());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Lightbringer");
    }

    @Test
    @DisplayName("Cannot target a black noncreature permanent")
    void cannotTargetBlackNonCreaturePermanent() {
        addLightbringer(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SealOfDoom());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Lightbringer");
    }

    @Test
    @DisplayName("Cannot activate Lightbringer while it is tapped")
    void cannotActivateWhileTapped() {
        Permanent lightbringer = addLightbringer(player1);
        Permanent target = addCreatureReady(player2, new SpinelessThug());

        lightbringer.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Lightbringer");
    }

    @Test
    @DisplayName("Can target a black creature it controls")
    void canTargetOwnBlackCreature() {
        addLightbringer(player1);
        Permanent target = addCreatureReady(player1, new SpinelessThug());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lightbringer");
        harness.assertNotOnBattlefield(player1, "Spineless Thug");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Spineless Thug"));
    }

    @Test
    @DisplayName("Ability fizzles if the black creature leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        addLightbringer(player1);
        Permanent target = addCreatureReady(player2, new SpinelessThug());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Lightbringer");
    }

    private Permanent addLightbringer(Player player) {
        return addCreatureReady(player, new Lightbringer());
    }
}
