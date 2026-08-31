package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Pillage.class, Ornithopter.class, Mountain.class, GrizzlyBears.class})
class PillageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Pillage destroys a target artifact and it can't be regenerated")
    void destroysTargetArtifact() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        thopter.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Pillage()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, thopter.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Resolving Pillage destroys a target land")
    void destroysTargetLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new Pillage()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, mountain.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Pillage cannot target a nonartifact creature")
    void cannotTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Pillage()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
