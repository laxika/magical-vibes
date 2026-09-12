package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SearingWind;
import com.github.laxika.magicalvibes.cards.v.VintaraSnapper;
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

@CardUsed({GlitteringLion.class, SearingWind.class, VintaraSnapper.class})
class GlitteringLionTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage that would be dealt to it")
    void preventsAllDamageToIt() {
        Permanent lion = addCreatureReady(player1, new GlitteringLion());
        harness.setHand(player2, List.of(new SearingWind()));
        harness.addMana(player2, ManaColor.RED, 9);

        harness.castAndResolveInstant(player2, 0, lion.getId());

        assertThat(lion.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lion);
    }

    @Test
    @DisplayName("Prevents combat damage that would be dealt to it")
    void preventsCombatDamageToIt() {
        Permanent lion = addCreatureReady(player1, new GlitteringLion());
        addCreatureReady(player2, new VintaraSnapper());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(lion.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lion);
        harness.assertInGraveyard(player2, "Vintara Snapper");
    }

    @Test
    @DisplayName("Any player may pay to turn off the prevention ability")
    void anyPlayerMayTurnOffPrevention() {
        Permanent lion = addCreatureReady(player1, new GlitteringLion());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new SearingWind()));
        harness.addMana(player2, ManaColor.RED, 9);
        harness.castAndResolveInstant(player2, 0, lion.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lion);
    }

    @Test
    @DisplayName("The controller may also pay to turn off the prevention ability")
    void controllerMayTurnOffPrevention() {
        Permanent lion = addCreatureReady(player1, new GlitteringLion());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new SearingWind()));
        harness.addMana(player2, ManaColor.RED, 9);
        harness.castAndResolveInstant(player2, 0, lion.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lion);
    }

    @Test
    @DisplayName("The prevention ability returns after end-of-turn cleanup")
    void preventionReturnsAfterEndOfTurn() {
        Permanent lion = addCreatureReady(player1, new GlitteringLion());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new SearingWind()));
        harness.addMana(player2, ManaColor.RED, 9);
        harness.castAndResolveInstant(player2, 0, lion.getId());

        assertThat(lion.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lion);
    }
}
