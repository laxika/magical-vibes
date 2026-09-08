package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FacelessDevourer.class, SoltariFootSoldier.class, GrizzlyBears.class, Unsummon.class})
class FacelessDevourerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles another creature with shadow until Faceless Devourer leaves")
    void etbExilesAnotherShadowCreature() {
        Permanent shadowCreature = harness.addToBattlefieldAndReturn(player2, new SoltariFootSoldier());
        castAndResolve(shadowCreature.getId());

        harness.assertNotOnBattlefield(player2, "Soltari Foot Soldier");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Soltari Foot Soldier"));
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();
    }

    @Test
    @DisplayName("Exiled creature returns when Faceless Devourer leaves")
    void exiledCreatureReturnsWhenSourceLeaves() {
        Permanent shadowCreature = harness.addToBattlefieldAndReturn(player2, new SoltariFootSoldier());
        castAndResolve(shadowCreature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        UUID sourceId = harness.getPermanentId(player1, "Faceless Devourer");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, sourceId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Soltari Foot Soldier");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Soltari Foot Soldier"));
    }

    @Test
    @DisplayName("ETB cannot target a creature without shadow")
    void etbRejectsCreatureWithoutShadow() {
        Permanent nonShadowCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FacelessDevourer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, nonShadowCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with shadow");
    }

    private void castAndResolve(UUID targetId) {
        harness.setHand(player1, List.of(new FacelessDevourer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
