package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LaquatussChampion.class, DoomBlade.class})
class LaquatussChampionTest extends BaseCardTest {

    @Test
    void targetedPlayerLosesLifeOnEnterAndRegainsItOnLeave() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castChampionWithTarget(player2.getId());

        harness.assertLife(player2, 14);

        destroyChampion();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void leavesTriggerUsesEtbTargetWhenEtbIsStillOnStack() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LaquatussChampion()));
        addChampionMana();
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        destroyChampion();
        harness.passBothPriorities();
        harness.assertLife(player2, 26);

        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }

    @Test
    void blackAbilityCreatesRegenerationShield() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new LaquatussChampion());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(champion.getRegenerationShield()).isEqualTo(1);
    }

    private void castChampionWithTarget(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new LaquatussChampion()));
        addChampionMana();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addChampionMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void destroyChampion() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Laquatus's Champion"));
        harness.passBothPriorities();
    }
}
