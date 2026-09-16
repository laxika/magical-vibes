package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThunderingSpineback;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuntingVelociraptor.class, ThunderingSpineback.class, GrizzlyBears.class})
class HuntingVelociraptorTest extends BaseCardTest {

    @Test
    @DisplayName("Dinosaur spells can be cast for granted prowl cost after qualifying combat damage")
    void dinosaurSpellCanUseGrantedProwl() {
        addSourceToBattlefield();
        recordDinosaurCombatDamage();
        harness.setHand(player1, List.of(new ThunderingSpineback()));
        harness.addMana(player1, ManaColor.RED, 3); // granted prowl {2}{R}, not normal {4}{G}{G}

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);

        harness.castWithProwl(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thundering Spineback");
    }

    @Test
    @DisplayName("Granted prowl requires qualifying combat damage")
    void grantedProwlRequiresCombatDamage() {
        addSourceToBattlefield();
        harness.setHand(player1, List.of(new ThunderingSpineback()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted prowl only applies to Dinosaur spells")
    void grantedProwlOnlyAppliesToDinosaurs() {
        addSourceToBattlefield();
        recordDinosaurCombatDamage();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addSourceToBattlefield() {
        harness.addToBattlefield(player1, new HuntingVelociraptor());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
    }

    private void recordDinosaurCombatDamage() {
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player1.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(CardSubtype.DINOSAUR);
    }
}
