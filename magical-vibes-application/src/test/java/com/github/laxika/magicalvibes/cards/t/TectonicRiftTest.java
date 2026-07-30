package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TectonicRiftTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys the target land")
    void destroysTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new TectonicRift()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Creatures without flying can't block this turn, fliers are unaffected")
    void nonFliersCantBlock() {
        harness.addToBattlefield(player2, new Forest());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent oppBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent oppHawk = addCreatureReady(player2, new SuntailHawk());

        harness.setHand(player1, List.of(new TectonicRift()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(ownBears.isCantBlockThisTurn()).isTrue();
        assertThat(oppBears.isCantBlockThisTurn()).isTrue();
        assertThat(oppHawk.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TectonicRift()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
