package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NullhideFerox.class, Distress.class, GrizzlyBears.class, Shock.class})
class NullhideFeroxTest extends BaseCardTest {

    @Test
    @DisplayName("Its controller can't cast noncreature spells, but opponents can")
    void restrictsOnlyItsController() {
        harness.addToBattlefieldAndReturn(player1, new NullhideFerox());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Any player may pay {2} to remove its abilities until end of turn")
    void anyPlayerCanRemoveItsAbilities() {
        Permanent nullhide = harness.addToBattlefieldAndReturn(player1, new NullhideFerox());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, nullhide, Keyword.HEXPROOF)).isFalse();
        assertThat(gs.getEffectiveActivatedAbilities(gd, nullhide)).isEmpty();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, nullhide, Keyword.HEXPROOF)).isTrue();
        assertThat(gs.getEffectiveActivatedAbilities(gd, nullhide)).hasSize(1);
    }

    @Test
    @DisplayName("Enters the battlefield instead of the graveyard when discarded by an opponent")
    void entersBattlefieldWhenDiscardedByOpponent() {
        harness.setHand(player1, List.of(new NullhideFerox()));
        harness.setHand(player2, List.of(new Distress()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player1, "Nullhide Ferox");
        harness.assertNotInGraveyard(player1, "Nullhide Ferox");
    }

    @Test
    @DisplayName("Its controller can still cast creature spells")
    void permitsCreatureSpells() {
        harness.addToBattlefield(player1, new NullhideFerox());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Self-caused discard puts it in the graveyard")
    void selfCausedDiscardDoesNotReplaceDestination() {
        harness.setHand(player1, List.of(new Distress(), new NullhideFerox()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Nullhide Ferox");
        harness.assertNotOnBattlefield(player1, "Nullhide Ferox");
    }

    @Test
    @DisplayName("Removing one Ferox's abilities leaves the other one's casting restriction active")
    void anotherFeroxStillRestrictsCasting() {
        harness.addToBattlefield(player1, new NullhideFerox());
        harness.addToBattlefield(player1, new NullhideFerox());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The ability cannot be activated again after abilities are removed")
    void cannotActivateAfterLosingAbilities() {
        harness.addToBattlefield(player1, new NullhideFerox());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
