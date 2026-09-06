package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrappleWithDeath;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClergyOfTheHolyNimbus.class, GrappleWithDeath.class, GrizzlyBears.class})
class ClergyOfTheHolyNimbusTest extends BaseCardTest {

    @Test
    @DisplayName("Intrinsic regeneration saves Clergy of the Holy Nimbus from destruction")
    void intrinsicRegenerationSavesFromDestruction() {
        Permanent clergy = addCreatureReady(player1, new ClergyOfTheHolyNimbus());

        harness.setHand(player2, List.of(new GrappleWithDeath()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addGrappleMana();

        harness.castSorcery(player2, 0, 0, clergy.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Clergy of the Holy Nimbus");
        assertThat(clergy.isTapped()).isTrue();
        assertThat(clergy.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Only an opponent can activate the regeneration-prevention ability")
    void onlyOpponentCanActivateAbility() {
        addCreatureReady(player1, new ClergyOfTheHolyNimbus());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only your opponents may activate this ability");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        Permanent clergy = findPermanent(player1, "Clergy of the Holy Nimbus");
        assertThat(clergy.isCantRegenerateThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The opponent's ability prevents intrinsic regeneration this turn")
    void abilityPreventsIntrinsicRegeneration() {
        Permanent clergy = addCreatureReady(player1, new ClergyOfTheHolyNimbus());

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new GrappleWithDeath()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addGrappleMana();
        harness.castSorcery(player2, 0, 0, clergy.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Clergy of the Holy Nimbus");
        harness.assertInGraveyard(player1, "Clergy of the Holy Nimbus");
    }

    private void addGrappleMana() {
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
    }
}
