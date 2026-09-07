package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({OjutaiExemplars.class, Shock.class, GrizzlyBears.class, Forest.class})
class OjutaiExemplarsTest extends BaseCardTest {

    private static final String TAP_MODE = "Tap target creature";
    private static final String KEYWORD_MODE =
            "Ojutai Exemplars gains first strike and lifelink until end of turn";
    private static final String FLICKER_MODE =
            "Exile Ojutai Exemplars, then return it to the battlefield tapped under its owner's control";

    @Test
    @DisplayName("Tap mode taps a target creature")
    void tapMode() {
        addReadyExemplars();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castShock();
        harness.handleListChoice(player1, TAP_MODE);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Keyword mode grants first strike and lifelink until end of turn")
    void keywordMode() {
        Permanent exemplars = addReadyExemplars();

        castShock();
        harness.handleListChoice(player1, KEYWORD_MODE);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, exemplars, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, exemplars, Keyword.LIFELINK)).isTrue();

        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, exemplars, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, exemplars, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Blink mode returns Ojutai Exemplars tapped under its owner's control")
    void blinkMode() {
        Permanent exemplars = addReadyExemplars();
        UUID oldId = exemplars.getId();

        castShock();
        harness.handleListChoice(player1, FLICKER_MODE);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Ojutai Exemplars");
        assertThat(returned.getId()).isNotEqualTo(oldId);
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Ojutai Exemplars")
    void creatureSpellDoesNotTrigger() {
        addReadyExemplars();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Tap mode rejects a noncreature target")
    void tapModeRejectsNoncreatureTarget() {
        addReadyExemplars();
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new Forest());

        castShock();
        harness.handleListChoice(player1, TAP_MODE);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyExemplars() {
        return addCreatureReady(player1, new OjutaiExemplars());
    }

    private void castShock() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
    }
}
