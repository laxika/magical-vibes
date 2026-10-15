package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Asphyxiate;
import com.github.laxika.magicalvibes.cards.b.BileBlight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ServantOfTymaret.class, Asphyxiate.class, BileBlight.class})
class ServantOfTymaretTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping Servant of Tymaret makes each opponent lose 1 life and its controller gain 1 life")
    void untapTriggerDrainsOpponent() {
        Permanent servant = harness.addToBattlefieldAndReturn(player1, new ServantOfTymaret());
        servant.tap();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Servant of Tymaret's activated ability grants it a regeneration shield")
    void activatedAbilityRegeneratesServant() {
        Permanent servant = harness.addToBattlefieldAndReturn(player1, new ServantOfTymaret());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(servant.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("An already untapped Servant does not trigger during the untap step")
    void alreadyUntappedServantDoesNotDrain() {
        harness.addToBattlefield(player1, new ServantOfTymaret());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Inspired drains the opponent of the Servant's controller")
    void opponentControlledServantDrainsPlayerOne() {
        Permanent servant = harness.addToBattlefieldAndReturn(player2, new ServantOfTymaret());
        servant.tap();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Regeneration replaces destruction, taps the Servant, and clears its damage")
    void regenerationSavesServantFromDestruction() {
        Permanent servant = harness.addToBattlefieldAndReturn(player1, new ServantOfTymaret());
        servant.setMarkedDamage(2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Asphyxiate()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castSorcery(player2, 0, servant.getId());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Servant of Tymaret");
        harness.assertNotInGraveyard(player1, "Servant of Tymaret");
        assertThat(servant.isTapped()).isTrue();
        assertThat(servant.getMarkedDamage()).isZero();
        assertThat(servant.getRegenerationShield()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A regeneration shield does not save the Servant from zero toughness")
    void regenerationDoesNotPreventZeroToughnessDeath() {
        Permanent servant = harness.addToBattlefieldAndReturn(player1, new ServantOfTymaret());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new BileBlight()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player2, 0, servant.getId());

        harness.assertNotOnBattlefield(player1, "Servant of Tymaret");
        harness.assertInGraveyard(player1, "Servant of Tymaret");
    }
}
