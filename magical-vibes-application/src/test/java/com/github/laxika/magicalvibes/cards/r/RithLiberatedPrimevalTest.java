package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ExquisiteFirecraft;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RithLiberatedPrimeval.class, ExquisiteFirecraft.class, HillGiant.class,
        ShivanDragon.class, Shock.class})
class RithLiberatedPrimevalTest extends BaseCardTest {

    @Test
    void createsDragonAtYourEndStepAfterExcessDamage() {
        harness.addToBattlefield(player1, new RithLiberatedPrimeval());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castFirecraft(target);

        advanceToEndStep();

        assertThat(findPermanents(player1, "Dragon").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
    }

    @Test
    void doesNotCreateDragonWithoutExcessDamage() {
        harness.addToBattlefield(player1, new RithLiberatedPrimeval());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        advanceToEndStep();

        assertThat(findPermanents(player1, "Dragon").stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
    }

    @Test
    void otherDragonsHaveWardTwo() {
        harness.addToBattlefield(player1, new RithLiberatedPrimeval());
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new ShivanDragon());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, dragon.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(dragon.getMarkedDamage()).isZero();
    }

    private void castFirecraft(Permanent target) {
        harness.setHand(player1, List.of(new ExquisiteFirecraft()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
