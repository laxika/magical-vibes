package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GlacialStalker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Backslide.class, GlacialStalker.class, GrizzlyBears.class})
class BackslideTest extends BaseCardTest {

    @Test
    void turnsTargetCreatureWithMorphFaceDown() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player2, new GlacialStalker());
        harness.setHand(player1, List.of(new Backslide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, stalker.getId());
        harness.passBothPriorities();

        assertThat(stalker.isFaceDown()).isTrue();
        assertThat(stalker.getEffectivePower()).isEqualTo(2);
        assertThat(stalker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void cannotTargetCreatureWithoutMorph() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Backslide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cyclingDiscardsBackslideAndDrawsACard() {
        harness.setHand(player1, List.of(new Backslide()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Backslide");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
