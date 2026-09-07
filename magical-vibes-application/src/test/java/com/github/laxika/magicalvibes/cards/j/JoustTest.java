package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
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

@CardUsed({Joust.class, YouthfulKnight.class, GrizzlyBears.class, LlanowarElves.class})
class JoustTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a Knight before it fights")
    void boostsKnightBeforeFight() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castJoust(knight, opponent);

        assertThat(knight.getEffectivePower()).isEqualTo(4);
        assertThat(knight.getEffectiveToughness()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Youthful Knight");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Does not boost a non-Knight before it fights")
    void doesNotBoostNonKnight() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castJoust(ownCreature, opponent);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The Knight's bonus lasts until end of turn")
    void bonusWearsOffAtEndOfTurn() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castJoust(knight, opponent);

        assertThat(knight.getEffectivePower()).isEqualTo(4);
        assertThat(knight.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(knight.getEffectivePower()).isEqualTo(2);
        assertThat(knight.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Requires a creature you control and a creature an opponent controls")
    void rejectsIllegalTargets() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherOwnCreature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Joust()));
        addJoustMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(opponent.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(ownCreature.getId(), otherOwnCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private void castJoust(Permanent ownCreature, Permanent opponent) {
        harness.setHand(player1, List.of(new Joust()));
        addJoustMana();
        harness.castAndResolveSorcery(player1, 0, List.of(ownCreature.getId(), opponent.getId()));
    }

    private void addJoustMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
