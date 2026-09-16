package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FoothillGuide;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IxidorRealitySculptor.class, FoothillGuide.class})
class IxidorRealitySculptorTest extends BaseCardTest {

    @Test
    void faceDownCreaturesGetPlusOnePlusOne() {
        Permanent ixidor = addCreatureReady(player1, new IxidorRealitySculptor());
        Permanent ownCreature = addCreatureReady(player1, new FoothillGuide());
        Permanent opposingCreature = addCreatureReady(player2, new FoothillGuide());
        ownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        opposingCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        assertThat(gqs.getEffectivePower(gd, ixidor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ixidor)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(3);
    }

    @Test
    void turnsTargetFaceDownCreatureFaceUpWithoutMorphCost() {
        addCreatureReady(player1, new IxidorRealitySculptor());
        Permanent target = addCreatureReady(player2, new FoothillGuide());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    void turnsOwnFaceDownCreatureFaceUp() {
        Permanent ixidor = addCreatureReady(player1, new IxidorRealitySculptor());
        Permanent target = addCreatureReady(player1, new FoothillGuide());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isFaceDown()).isFalse();
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(ixidor.isTapped()).isFalse();
    }

    @Test
    void cannotTargetFaceUpCreature() {
        addCreatureReady(player1, new IxidorRealitySculptor());
        Permanent target = addCreatureReady(player2, new FoothillGuide());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
