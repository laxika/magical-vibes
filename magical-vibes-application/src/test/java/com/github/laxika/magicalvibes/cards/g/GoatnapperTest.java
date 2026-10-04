package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Goatnapper.class, AvianChangeling.class, Tarfire.class})
class GoatnapperTest extends BaseCardTest {

    private void castGoatnapper(UUID targetId) {
        harness.setHand(player1, List.of(new Goatnapper()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, targetId);
    }

    @Test
    @DisplayName("ETB trigger goes on the stack targeting the Goat")
    void etbTriggersOnStack() {
        harness.addToBattlefield(player2, new AvianChangeling());
        UUID targetId = harness.getPermanentId(player2, "Avian Changeling");
        castGoatnapper(targetId);

        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Goatnapper");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Untaps, steals until end of turn and grants haste to the Goat")
    void stealsUntapsAndGrantsHaste() {
        Permanent goat = harness.addToBattlefieldAndReturn(player2, new AvianChangeling());
        goat.tap();
        castGoatnapper(goat.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(goat.isTapped()).isFalse();
        assertThat(goat.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertOnBattlefield(player1, "Avian Changeling");
        harness.assertNotOnBattlefield(player2, "Avian Changeling");
        assertThat(gd.isStolenUntilEndOfTurn(goat.getId())).isTrue();
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent goat = harness.addToBattlefieldAndReturn(player2, new AvianChangeling());
        castGoatnapper(goat.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(goat.hasKeyword(Keyword.HASTE)).isFalse();
        harness.assertOnBattlefield(player2, "Avian Changeling");
        harness.assertNotOnBattlefield(player1, "Avian Changeling");
        assertThat(gd.isStolenUntilEndOfTurn(goat.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-Goat creature")
    void cannotTargetNonGoat() {
        harness.addToBattlefield(player2, new Goatnapper());
        UUID targetId = harness.getPermanentId(player2, "Goatnapper");
        harness.setHand(player1, List.of(new Goatnapper()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No ETB trigger when cast with no Goat to target")
    void noTriggerWithoutGoat() {
        castGoatnapper(null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goatnapper");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can untap and grant haste to a Goat already under your control")
    void canTargetOwnGoat() {
        Permanent goat = harness.addToBattlefieldAndReturn(player1, new AvianChangeling());
        goat.tap();
        castGoatnapper(goat.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(goat.isTapped()).isFalse();
        assertThat(goat.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertOnBattlefield(player1, "Avian Changeling");
        harness.assertNotOnBattlefield(player2, "Avian Changeling");
    }

    @Test
    @DisplayName("Ability has no effect when its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent goat = harness.addToBattlefieldAndReturn(player2, new AvianChangeling());
        castGoatnapper(goat.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, goat.getId());
        harness.assertInGraveyard(player2, "Avian Changeling");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Avian Changeling");
        harness.assertNotOnBattlefield(player2, "Avian Changeling");
        assertThat(gd.isStolenUntilEndOfTurn(goat.getId())).isFalse();
    }

    @Test
    @DisplayName("Ability still resolves after Goatnapper leaves the battlefield")
    void sourceLeavesBeforeResolution() {
        Permanent goat = harness.addToBattlefieldAndReturn(player2, new AvianChangeling());
        goat.tap();
        castGoatnapper(goat.getId());
        harness.passBothPriorities();

        UUID sourceId = harness.getPermanentId(player1, "Goatnapper");
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, sourceId);
        harness.assertInGraveyard(player1, "Goatnapper");
        harness.passBothPriorities();

        assertThat(goat.isTapped()).isFalse();
        assertThat(goat.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertOnBattlefield(player1, "Avian Changeling");
        harness.assertNotOnBattlefield(player2, "Avian Changeling");

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(goat.hasKeyword(Keyword.HASTE)).isFalse();
        harness.assertOnBattlefield(player2, "Avian Changeling");
        harness.assertNotOnBattlefield(player1, "Avian Changeling");
    }
}
