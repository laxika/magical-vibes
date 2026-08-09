package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CanyonMinotaur;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LawbringerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Lawbringer sacrifices it and exiles a red creature")
    void sacrificesSelfAndExilesRedCreature() {
        addLawbringer(player1);
        Permanent target = addPermanent(player2, new CanyonMinotaur());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Lawbringer");
        harness.assertOnBattlefield(player2, "Canyon Minotaur");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Canyon Minotaur");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Canyon Minotaur"));
    }

    @Test
    @DisplayName("Cannot target a non-red creature")
    void cannotTargetNonRedCreature() {
        addLawbringer(player1);
        Permanent target = addPermanent(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if the red creature leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        addLawbringer(player1);
        Permanent target = addPermanent(player2, new CanyonMinotaur());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Lawbringer");
    }

    private Permanent addLawbringer(Player player) {
        return addPermanent(player, new Lawbringer());
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
