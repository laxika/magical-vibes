package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AetherFlashTest extends BaseCardTest {

    @Test
    @DisplayName("A creature entering with 2 or less toughness is destroyed by the 2 damage")
    void destroysSmallEnteringCreature() {
        harness.addToBattlefield(player1, new AetherFlash());

        harness.setHand(player1, List.of(new GrizzlyBears())); // 2/2
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell → Aether Flash triggers
        harness.passBothPriorities(); // resolve trigger → 2 damage → lethal to a 2/2

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("A tougher creature survives but keeps 2 marked damage")
    void toughCreatureSurvivesWithMarkedDamage() {
        harness.addToBattlefield(player1, new AetherFlash());

        harness.setHand(player1, List.of(new HillGiant())); // 3/3
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell → trigger
        harness.passBothPriorities(); // resolve trigger → 2 damage marked

        Permanent hillGiant = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hill Giant"))
                .findFirst().orElseThrow();
        assertThat(hillGiant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fires for a creature entering under an opponent's control")
    void firesForOpponentCreature() {
        harness.addToBattlefield(player1, new AetherFlash());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        harness.passBothPriorities(); // resolve creature spell → trigger
        harness.passBothPriorities(); // resolve trigger → 2 damage → lethal to a 2/2

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
