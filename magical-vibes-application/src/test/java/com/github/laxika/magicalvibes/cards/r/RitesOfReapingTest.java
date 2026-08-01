package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RitesOfReapingTest extends BaseCardTest {

    @Test
    @DisplayName("First target gets +3/+3 and second gets -3/-3")
    void boostsFirstAndDebuffsSecond() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RitesOfReaping()));
        addCastMana();

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castSorcery(player1, 0, List.of(bearsId, elvesId));
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Rites of Reaping");
    }

    @Test
    @DisplayName("Both modifiers wear off at cleanup")
    void modifiersWearOffAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new RitesOfReaping()));
        addCastMana();

        UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID oppId = harness.getPermanentId(player2, "Air Elemental");
        harness.castSorcery(player1, 0, List.of(ownId, oppId));
        harness.passBothPriorities();

        Permanent own = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent opp = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(own.getEffectivePower()).isEqualTo(5);
        assertThat(opp.getEffectivePower()).isEqualTo(1);
        assertThat(opp.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(own.getEffectivePower()).isEqualTo(2);
        assertThat(own.getEffectiveToughness()).isEqualTo(2);
        assertThat(opp.getEffectivePower()).isEqualTo(4);
        assertThat(opp.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot choose the same creature for both targets")
    void cannotTargetSameCreatureTwice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RitesOfReaping()));
        addCastMana();

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearsId, bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new RitesOfReaping()));
        addCastMana();

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearsId, fountainId)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addCastMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
