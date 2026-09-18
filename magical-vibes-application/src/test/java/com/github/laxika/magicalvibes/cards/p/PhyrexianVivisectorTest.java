package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianVivisector.class, GrizzlyBears.class, Shock.class})
class PhyrexianVivisectorTest extends BaseCardTest {

    @Test
    @DisplayName("An ally creature dying causes you to scry 1")
    void allyCreatureDeathCausesScry() {
        harness.addToBattlefield(player1, new PhyrexianVivisector());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        killWithShock(player1, creature);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Phyrexian Vivisector's own death causes you to scry 1")
    void ownDeathCausesScry() {
        Permanent vivisector = harness.addToBattlefieldAndReturn(player1, new PhyrexianVivisector());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        killWithShock(player1, vivisector);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's creature dying does not cause you to scry")
    void opponentCreatureDeathDoesNotCauseScry() {
        harness.addToBattlefield(player1, new PhyrexianVivisector());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithShock(player1, creature);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    private void killWithShock(Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
