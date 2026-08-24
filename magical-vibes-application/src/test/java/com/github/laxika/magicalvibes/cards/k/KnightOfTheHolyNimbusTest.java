package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrappleWithDeath;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnightOfTheHolyNimbus.class, GrappleWithDeath.class, GrizzlyBears.class})
class KnightOfTheHolyNimbusTest extends BaseCardTest {

    @Test
    @DisplayName("Flanking gives a non-flanking blocker -1/-1")
    void flankingHitsNonFlankingBlocker() {
        Permanent knight = addCreatureReady(player1, new KnightOfTheHolyNimbus());
        knight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Intrinsic regeneration saves Knight of the Holy Nimbus from destruction")
    void intrinsicRegenerationSavesFromDestruction() {
        Permanent knight = addCreatureReady(player1, new KnightOfTheHolyNimbus());

        harness.setHand(player2, List.of(new GrappleWithDeath()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addGrappleMana();

        harness.castSorcery(player2, 0, 0, knight.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Knight of the Holy Nimbus");
        assertThat(knight.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only an opponent can activate the regeneration-prevention ability")
    void onlyOpponentCanActivateAbility() {
        addCreatureReady(player1, new KnightOfTheHolyNimbus());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only your opponents may activate this ability");

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Knight of the Holy Nimbus");
        assertThat(knight.isCantRegenerateThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The opponent's ability prevents intrinsic regeneration this turn")
    void abilityPreventsIntrinsicRegeneration() {
        Permanent knight = addCreatureReady(player1, new KnightOfTheHolyNimbus());

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new GrappleWithDeath()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addGrappleMana();
        harness.castSorcery(player2, 0, 0, knight.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Knight of the Holy Nimbus");
        harness.assertInGraveyard(player1, "Knight of the Holy Nimbus");
    }

    private void addGrappleMana() {
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
    }
}
