package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrongholdAssassin.class, SkyshroudTroopers.class, VolrathsStronghold.class})
class StrongholdAssassinTest extends BaseCardTest {

    private Permanent setup() {
        return addCreatureReady(player1, new StrongholdAssassin());
    }

    private int idxOf(Permanent p) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(p);
    }

    @Test
    @DisplayName("Taps, sacrifices a creature, and destroys the target nonblack creature")
    void destroysNonblackTarget() {
        Permanent assassin = setup();
        Permanent fodder = addCreatureReady(player1, new SkyshroudTroopers());
        Permanent target = addCreatureReady(player2, new SkyshroudTroopers());

        harness.activateAbility(player1, idxOf(assassin), 0, null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        // Fodder sacrificed as cost, assassin tapped
        harness.assertInGraveyard(player1, "Skyshroud Troopers");
        assertThat(assassin.isTapped()).isTrue();

        // Target destroyed
        harness.assertNotOnBattlefield(player2, "Skyshroud Troopers");
        harness.assertInGraveyard(player2, "Skyshroud Troopers");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent assassin = setup();
        Permanent blackCreature = addCreatureReady(player2, new StrongholdAssassin());

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(assassin), 0, null, blackCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent assassin = setup();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new VolrathsStronghold());

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(assassin), 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player — the ability only targets creatures")
    void cannotTargetPlayer() {
        Permanent assassin = setup();

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(assassin), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May sacrifice the source creature as the activation cost")
    void maySacrificeSourceAsCost() {
        Permanent assassin = setup();
        Permanent target = addCreatureReady(player1, new SkyshroudTroopers());

        harness.activateAbility(player1, idxOf(assassin), 0, null, target.getId());
        harness.handlePermanentChosen(player1, assassin.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Stronghold Assassin");
        harness.assertInGraveyard(player1, "Skyshroud Troopers");
        harness.assertNotOnBattlefield(player1, "Stronghold Assassin");
        harness.assertNotOnBattlefield(player1, "Skyshroud Troopers");
    }
}
