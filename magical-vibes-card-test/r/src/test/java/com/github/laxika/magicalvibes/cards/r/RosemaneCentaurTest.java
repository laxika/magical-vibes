package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RosemaneCentaur.class, VernadiShieldmate.class})
class RosemaneCentaurTest extends BaseCardTest {

    @Test
    @DisplayName("Convoke taps creatures to help pay the cost")
    void castsWithConvoke() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        harness.setHand(player1, List.of(new RosemaneCentaur()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Rosemane Centaur")).isEqualTo(1);
    }

    @Test
    @DisplayName("Summoning-sick multicolored creatures can convoke both colored requirements")
    void convokesColoredManaWithSummoningSickCreatures() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        firstCreature.setSummoningSick(true);
        secondCreature.setSummoningSick(true);
        harness.setHand(player1, List.of(new RosemaneCentaur()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Rosemane Centaur");
    }

    @Test
    @DisplayName("Convoke is optional when paying the entire cost with mana")
    void castsWithoutConvoke() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        harness.setHand(player1, List.of(new RosemaneCentaur()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rosemane Centaur");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Vigilance lets Rosemane Centaur attack without tapping")
    void attacksWithoutTapping() {
        Permanent centaur = addCreatureReady(player1, new RosemaneCentaur());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(centaur.isAttacking()).isTrue();
        assertThat(centaur.isTapped()).isFalse();
        resolveCombat();
        harness.assertLife(player2, 16);
    }
}
