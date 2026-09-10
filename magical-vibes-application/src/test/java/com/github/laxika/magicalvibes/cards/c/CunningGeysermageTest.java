package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CunningGeysermage.class, GrizzlyBears.class, FountainOfYouth.class})
class CunningGeysermageTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotReturnACreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CunningGeysermage()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(target).isIn(gd.playerBattlefields.get(player2.getId()));
        harness.assertOnBattlefield(player1, "Cunning Geysermage");
    }

    @Test
    void kickedReturnsTheTargetCreatureToItsOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CunningGeysermage()));
        addKickedMana();

        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Cunning Geysermage");
    }

    @Test
    void kickedMayChooseNoTarget() {
        harness.setHand(player1, List.of(new CunningGeysermage()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cunning Geysermage");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void kickedCannotTargetANoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new CunningGeysermage()));
        addKickedMana();

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
