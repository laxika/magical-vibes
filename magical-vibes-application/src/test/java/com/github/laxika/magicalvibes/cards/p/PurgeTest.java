package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.cards.s.SilverMyr;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Purge destroys a target artifact creature")
    void destroysArtifactCreature() {
        harness.addToBattlefield(player2, new SilverMyr());
        harness.setHand(player1, List.of(new Purge()));
        addPurgeMana();

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Silver Myr"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Silver Myr");
        harness.assertInGraveyard(player2, "Silver Myr");
    }

    @Test
    @DisplayName("Purge destroys a target black creature and ignores regeneration")
    void destroysBlackCreatureWithoutRegeneration() {
        Permanent ghouls = harness.addToBattlefieldAndReturn(player2, new MassOfGhouls());
        ghouls.setRegenerationShield(1);
        harness.setHand(player1, List.of(new Purge()));
        addPurgeMana();

        harness.castInstant(player1, 0, ghouls.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mass of Ghouls");
        harness.assertInGraveyard(player2, "Mass of Ghouls");
    }

    @Test
    @DisplayName("Purge cannot target a noncreature artifact")
    void cannotTargetNoncreatureArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Purge()));
        addPurgeMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Fountain of Youth")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact creature or black creature");
    }

    private void addPurgeMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
