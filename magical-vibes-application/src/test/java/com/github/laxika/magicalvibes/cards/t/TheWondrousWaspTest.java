package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.m.MindStone;
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

@CardUsed({TheWondrousWasp.class, AirElemental.class, MindStone.class, Unsummon.class})
class TheWondrousWaspTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps a target creature and removes its abilities while the Wasp remains")
    void etbTapsAndRemovesAbilities() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castWasp(elemental.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(elemental.isTapped()).isTrue();
        assertThat(gqs.hasLostAllAbilities(gd, elemental)).isTrue();
    }

    @Test
    @DisplayName("The target regains its abilities when The Wondrous Wasp leaves")
    void abilitiesReturnWhenWaspLeaves() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castWasp(elemental.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent wasp = findPermanent(player1, "The Wondrous Wasp");
        bounceWasp(wasp.getId());

        assertThat(gqs.hasLostAllAbilities(gd, elemental)).isFalse();
    }

    @Test
    @DisplayName("If the Wasp leaves before its ETB resolves, the creature is tapped but keeps its abilities")
    void sourceLeavesBeforeEtbResolution() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castWasp(elemental.getId());
        harness.passBothPriorities();

        Permanent wasp = findPermanent(player1, "The Wondrous Wasp");
        bounceWasp(wasp.getId());
        harness.passBothPriorities();

        assertThat(elemental.isTapped()).isTrue();
        assertThat(gqs.hasLostAllAbilities(gd, elemental)).isFalse();
    }

    @Test
    @DisplayName("The ETB trigger may resolve without a target")
    void etbMayHaveNoTarget() {
        castWasp(null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Wondrous Wasp");
    }

    @Test
    @DisplayName("A creature you control may be targeted")
    void mayTargetOwnCreature() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        castWasp(elemental.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(elemental.isTapped()).isTrue();
        assertThat(gqs.hasLostAllAbilities(gd, elemental)).isTrue();
    }

    @Test
    @DisplayName("The ETB trigger cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent mindStone = harness.addToBattlefieldAndReturn(player2, new MindStone());

        harness.setHand(player1, List.of(new TheWondrousWasp()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, mindStone.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWasp(UUID targetId) {
        harness.setHand(player1, List.of(new TheWondrousWasp()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        if (targetId == null) {
            harness.castCreature(player1, 0);
        } else {
            harness.castCreature(player1, 0, 0, targetId);
        }
    }

    private void bounceWasp(UUID waspId) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, waspId);
        harness.passBothPriorities();
    }
}
