package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrushUnderfootTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen Giant deals damage equal to its power to target creature, killing it")
    void giantKillsTargetCreature() {
        // Hill Giant (3/3) deals 3 damage to Grizzly Bears (2/2)
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CrushUnderfoot()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(giantId, bearsId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Target creature survives when Giant's power is less than its toughness")
    void targetSurvivesLesserDamage() {
        // Hill Giant (3/3) deals 3 damage to Giant Spider (2/4) — survives with 3 damage
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new CrushUnderfoot()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        UUID targetId = harness.getPermanentId(player2, "Giant Spider");
        harness.castInstant(player1, 0, List.of(giantId, targetId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Cannot choose a non-Giant creature you control")
    void cannotChooseNonGiant() {
        harness.addToBattlefield(player1, new HillGiant()); // gives a legal Giant so the spell is castable
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CrushUnderfoot()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearsId, targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Giant creature you control");
    }

    @Test
    @DisplayName("Cannot choose an opponent's Giant as the source")
    void cannotChooseOpponentGiant() {
        harness.addToBattlefield(player1, new HillGiant()); // needed so the spell is castable
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CrushUnderfoot()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID opponentGiantId = harness.getPermanentId(player2, "Hill Giant");
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(opponentGiantId, targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Giant creature you control");
    }
}
