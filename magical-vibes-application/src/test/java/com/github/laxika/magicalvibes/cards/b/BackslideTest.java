package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DaruHealer;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Backslide.class, DaruHealer.class, GlorySeeker.class})
class BackslideTest extends BaseCardTest {

    @Test
    void turnsTargetCreatureWithMorphFaceDown() {
        Permanent healer = harness.addToBattlefieldAndReturn(player2, new DaruHealer());
        harness.setHand(player1, List.of(new Backslide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, healer.getId());

        assertThat(healer.isFaceDown()).isTrue();
        assertThat(healer.getEffectivePower()).isEqualTo(2);
        assertThat(healer.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void cannotTargetCreatureWithoutMorph() {
        Permanent glorySeeker = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new Backslide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, glorySeeker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetCreatureAlreadyFaceDown() {
        Permanent faceDownHealer = harness.addToBattlefieldAndReturn(player2, new DaruHealer());
        faceDownHealer.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new Backslide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, faceDownHealer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cyclingDiscardsBackslideAndDrawsACard() {
        harness.setHand(player1, List.of(new Backslide()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Backslide");
        harness.assertInHand(player1, "Glory Seeker");
    }
}
