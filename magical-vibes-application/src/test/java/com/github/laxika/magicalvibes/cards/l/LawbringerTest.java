package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.m.MoggToady;
import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Lawbringer.class, ChandraNalaar.class, MoggToady.class, Mossdog.class})
class LawbringerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Lawbringer sacrifices it and exiles a red creature")
    void sacrificesSelfAndExilesRedCreature() {
        addLawbringer(player1);
        Permanent target = addCreatureReady(player2, new MoggToady());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Lawbringer");
        harness.assertOnBattlefield(player2, "Mogg Toady");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mogg Toady");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Mogg Toady"));
    }

    @Test
    @DisplayName("Cannot target a non-red creature")
    void cannotTargetNonRedCreature() {
        addLawbringer(player1);
        Permanent target = addCreatureReady(player2, new Mossdog());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Lawbringer");
    }

    @Test
    @DisplayName("Cannot target a red noncreature permanent")
    void cannotTargetRedNonCreaturePermanent() {
        addLawbringer(player1);
        Permanent target = addCreatureReady(player2, new ChandraNalaar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Lawbringer");
    }

    @Test
    @DisplayName("Cannot activate Lawbringer while it is tapped")
    void cannotActivateWhileTapped() {
        Permanent lawbringer = addLawbringer(player1);
        Permanent target = addCreatureReady(player2, new MoggToady());

        lawbringer.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Lawbringer");
    }

    @Test
    @DisplayName("Ability fizzles if the red creature leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        addLawbringer(player1);
        Permanent target = addCreatureReady(player2, new MoggToady());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Lawbringer");
    }

    private Permanent addLawbringer(Player player) {
        return addCreatureReady(player, new Lawbringer());
    }
}
