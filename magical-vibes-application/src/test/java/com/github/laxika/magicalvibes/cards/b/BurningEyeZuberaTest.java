package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SpiralingEmbers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurningEyeZubera.class, SpiralingEmbers.class})
class BurningEyeZuberaTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a target when it dies after being dealt 4 damage")
    void dealsThreeDamageAfterBeingDealtFourDamage() {
        Permanent zubera = harness.addToBattlefieldAndReturn(player1, new BurningEyeZubera());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(
                new SpiralingEmbers(), new SpiralingEmbers(), new SpiralingEmbers(),
                new SpiralingEmbers(), new SpiralingEmbers()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player2, 0, zubera.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Burning-Eye Zubera");
    }

    @Test
    @DisplayName("Does not deal damage when it dies after being dealt less than 4 damage")
    void doesNotDealDamageAfterBeingDealtLessThanFourDamage() {
        Permanent zubera = harness.addToBattlefieldAndReturn(player1, new BurningEyeZubera());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(
                new SpiralingEmbers(), new SpiralingEmbers(), new SpiralingEmbers(),
                new SpiralingEmbers()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player2, 0, zubera.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Burning-Eye Zubera");
    }

    @Test
    @DisplayName("Deals 3 damage to a target creature")
    void dealsThreeDamageToTargetCreature() {
        Permanent zubera = harness.addToBattlefieldAndReturn(player1, new BurningEyeZubera());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BurningEyeZubera());
        harness.setHand(player2, List.of(
                new SpiralingEmbers(), new SpiralingEmbers(), new SpiralingEmbers(),
                new SpiralingEmbers(), new SpiralingEmbers()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player2, 0, zubera.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Burning-Eye Zubera");
        harness.assertInGraveyard(player2, "Burning-Eye Zubera");
    }
}
