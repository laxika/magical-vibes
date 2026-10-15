package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreadPresence.class, Swamp.class, Forest.class})
class DreadPresenceTest extends BaseCardTest {

    private static final String DRAW = "You draw a card and you lose 1 life.";
    private static final String DAMAGE = "This creature deals 2 damage to any target and you gain 2 life.";

    @Test
    @DisplayName("Swamp landfall draws a card and loses 1 life when that mode is chosen")
    void drawMode() {
        harness.addToBattlefield(player1, new DreadPresence());
        harness.setHand(player1, List.of(new Swamp()));
        harness.setLife(player1, 20);

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, DRAW);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Swamp landfall damages any target and gains 2 life when that mode is chosen")
    void damageMode() {
        harness.addToBattlefield(player1, new DreadPresence());
        harness.setHand(player1, List.of(new Swamp()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, DAMAGE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A non-Swamp land does not trigger Dread Presence")
    void nonSwampDoesNotTrigger() {
        harness.addToBattlefield(player1, new DreadPresence());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player1, 20);

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Swamp does not trigger Dread Presence")
    void opponentsSwampDoesNotTrigger() {
        harness.addToBattlefield(player1, new DreadPresence());

        harness.enterBattlefieldAndReturn(player2, new Swamp());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A Swamp entering without being played still triggers the draw mode")
    void swampEnteringWithoutBeingPlayedTriggers() {
        harness.addToBattlefield(player1, new DreadPresence());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player1, new Swamp());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextTriggeredModalTrigger(gd));
        harness.handleListChoice(player1, DRAW);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("The damage mode can target a creature and gains life independently")
    void damageModeTargetsCreature() {
        harness.addToBattlefield(player1, new DreadPresence());
        var target = harness.addToBattlefieldAndReturn(player2, new DreadPresence());
        harness.setHand(player1, List.of(new Swamp()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, DAMAGE);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Dread Presence");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An illegal sole damage target prevents the life gain too")
    void illegalDamageTargetPreventsLifeGain() {
        harness.addToBattlefield(player1, new DreadPresence());
        var target = harness.addToBattlefieldAndReturn(player2, new DreadPresence());
        harness.setHand(player1, List.of(new Swamp()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, DAMAGE);
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, target));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInHand(player2, "Dread Presence");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The damage trigger resolves even if Dread Presence leaves the battlefield")
    void damageTriggerSurvivesSourceRemoval() {
        var source = harness.addToBattlefieldAndReturn(player1, new DreadPresence());
        harness.setHand(player1, List.of(new Swamp()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, DAMAGE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, source));
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.assertInHand(player1, "Dread Presence");
    }
}
