package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlackDragon.class, GrizzlyBears.class, HillGiant.class})
class BlackDragonTest extends BaseCardTest {

    @Test
    void etbGivesTargetOpponentCreatureMinusThreeMinusThree() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castBlackDragon(giant.getId());

        assertThat(giant.getPowerModifier()).isEqualTo(-3);
        assertThat(giant.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    void etbMinusThreeMinusThreeKillsSmallCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castBlackDragon(targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void etbCannotTargetCreatureItsControllerControls() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlackDragon()));
        addBlackDragonMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canEnterWithoutAnOpponentCreatureToTarget() {
        harness.setHand(player1, List.of(new BlackDragon()));
        addBlackDragonMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Black Dragon");
    }

    private void castBlackDragon(UUID targetId) {
        harness.setHand(player1, List.of(new BlackDragon()));
        addBlackDragonMana();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addBlackDragonMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
