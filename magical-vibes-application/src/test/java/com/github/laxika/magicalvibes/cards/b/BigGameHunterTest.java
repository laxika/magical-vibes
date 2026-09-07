package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BigGameHunter.class, AirElemental.class, HillGiant.class, RavensCrime.class})
class BigGameHunterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys a target creature with power 4 or greater and prevents regeneration")
    void etbDestroysHighPowerCreatureWithoutRegeneration() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        target.setRegenerationShield(1);
        harness.setHand(player1, List.of(new BigGameHunter()));
        addBigGameHunterMana(player1);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetLowPowerCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new BigGameHunter()));
        addBigGameHunterMana(player1);

        assertThatThrownBy(() -> harness.getGameService()
                .playCard(harness.getGameData(), player1, 0, 0,
                        harness.getPermanentId(player2, "Hill Giant"), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Madness casts Big Game Hunter for {B}")
    void madnessCastsBigGameHunter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        BigGameHunter hunter = new BigGameHunter();
        harness.setHand(player1, List.of(hunter));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertOnBattlefield(player1, "Big Game Hunter");
    }

    private void addBigGameHunterMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
