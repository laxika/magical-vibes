package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BloodsoakedChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodsoaked Champion can't block")
    void cantBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new BloodsoakedChampion());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Raid returns Bloodsoaked Champion from the graveyard to the battlefield")
    void raidReturnsFromGraveyard() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new BloodsoakedChampion()));

        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bloodsoaked Champion");
    }

    @Test
    @DisplayName("Bloodsoaked Champion can't use its raid ability without attacking")
    void raidRequiresAttacking() {
        harness.setGraveyard(player1, List.of(new BloodsoakedChampion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }
}
