package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LightbringerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Lightbringer sacrifices it and exiles a black creature")
    void sacrificesSelfAndExilesBlackCreature() {
        addLightbringer(player1);
        Permanent target = addPermanent(player2, new ScatheZombies());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Lightbringer");
        harness.assertOnBattlefield(player2, "Scathe Zombies");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Scathe Zombies");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Scathe Zombies"));
    }

    @Test
    @DisplayName("Cannot target a non-black creature")
    void cannotTargetNonBlackCreature() {
        addLightbringer(player1);
        Permanent target = addPermanent(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if the black creature leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        addLightbringer(player1);
        Permanent target = addPermanent(player2, new ScatheZombies());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Lightbringer");
    }

    private Permanent addLightbringer(Player player) {
        return addPermanent(player, new Lightbringer());
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
