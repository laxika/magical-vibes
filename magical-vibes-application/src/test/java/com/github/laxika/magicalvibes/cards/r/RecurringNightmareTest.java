package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Coercion;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecurringNightmareTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature, returns itself to hand, and reanimates the target")
    void sacrificesBouncesAndReanimates() {
        harness.addToBattlefield(player1, new RecurringNightmare());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card returned = new LlanowarElves();
        harness.setGraveyard(player1, List.of(returned));

        harness.activateAbility(player1, 0, null, returned.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Recurring Nightmare")).isZero();
        harness.assertInHand(player1, "Recurring Nightmare");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Llanowar Elves")).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        harness.addToBattlefield(player1, new RecurringNightmare());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card noncreature = new Coercion();
        harness.setGraveyard(player1, List.of(noncreature));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, noncreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate only at sorcery speed")
    void cannotActivateOutsideMainPhase() {
        harness.addToBattlefield(player1, new RecurringNightmare());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card returned = new LlanowarElves();
        harness.setGraveyard(player1, List.of(returned));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, returned.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }
}
