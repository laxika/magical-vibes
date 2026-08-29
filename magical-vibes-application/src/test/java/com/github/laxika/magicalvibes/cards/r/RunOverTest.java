package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BrightfieldMustang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RunOverTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {G} and deals Mount's power to an opponent's creature")
    void costsLessWhenTargetingMountYouControl() {
        harness.addToBattlefield(player1, new BrightfieldMustang());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RunOver()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID mustangId = harness.getPermanentId(player1, "Brightfield Mustang");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(mustangId, elvesId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Requires the full cost when the first target is not a Mount or Vehicle")
    void doesNotReduceCostForOtherCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RunOver()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearsId, elvesId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature you control as the second target")
    void cannotTargetOwnCreatureAsSecondTarget() {
        GrizzlyBears source = new GrizzlyBears();
        GrizzlyBears victim = new GrizzlyBears();
        harness.addToBattlefield(player1, source);
        harness.addToBattlefield(player1, victim);
        harness.setHand(player1, List.of(new RunOver()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        List<Permanent> battlefield = harness.getGameData().playerBattlefields.get(player1.getId());
        UUID sourceId = battlefield.get(0).getId();
        UUID victimId = battlefield.get(1).getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(sourceId, victimId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }
}
