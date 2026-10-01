package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeepSlumberTitan.class, FlameJavelin.class})
class DeepSlumberTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Deep-Slumber Titan enters the battlefield tapped")
    void entersTapped() {
        Permanent titan = harness.enterBattlefieldAndReturn(player1, new DeepSlumberTitan());

        assertThat(titan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped Deep-Slumber Titan does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent titan = addCreatureReady(player1, new DeepSlumberTitan());
        titan.tap();

        advanceToUpkeep(player1);

        assertThat(titan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("When Deep-Slumber Titan is dealt damage, it untaps (and survives as a 7/7)")
    void untapsWhenDealtDamage() {
        Permanent titan = addCreatureReady(player2, new DeepSlumberTitan());
        titan.tap();

        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID titanId = titan.getId();
        harness.castInstant(player1, 0, titanId);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(titan.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Deep-Slumber Titan");
        assertThat(titan.isTapped()).isFalse();
    }
}
