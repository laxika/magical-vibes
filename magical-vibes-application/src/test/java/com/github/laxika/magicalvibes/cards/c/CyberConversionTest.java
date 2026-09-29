package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CyberConversion.class, Forest.class, GrizzlyBears.class})
class CyberConversionTest extends BaseCardTest {

    @Test
    void turnsTargetCreatureFaceDownAsCyberman() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castCyberConversion(target);

        assertThat(target.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardTypes(gd, target))
                .containsExactlyInAnyOrder(CardType.ARTIFACT, CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target))
                .containsExactly(CardSubtype.CYBERMAN);
    }

    @Test
    void turningThePermanentFaceUpRestoresItsOriginalCharacteristics() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castCyberConversion(target);

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, target);
        harness.passBothPriorities();

        assertThat(target.isFaceDown()).isFalse();
        assertThat(gqs.getEffectiveCardTypes(gd, target)).containsExactly(CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.BEAR);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new CyberConversion()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castCyberConversion(Permanent target) {
        harness.setHand(player1, List.of(new CyberConversion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
