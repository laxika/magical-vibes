package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattleMenu.class, CrawWurm.class, GrizzlyBears.class})
class BattleMenuTest extends BaseCardTest {

    @Test
    @DisplayName("Attack creates a 2/2 white Knight token")
    void attackCreatesKnightToken() {
        cast(0, null);

        Permanent knight = findPermanent(player1, "Knight");
        assertThat(knight.getCard().isToken()).isTrue();
        assertThat(knight.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(knight.getCard().getSubtypes()).containsExactly(CardSubtype.KNIGHT);
        assertThat(knight.getEffectivePower()).isEqualTo(2);
        assertThat(knight.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability gives a target creature +0/+4 until end of turn")
    void abilityBoostsCreatureUntilEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(1, creature.getId());

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Magic destroys a creature with power 4 or greater")
    void magicDestroysLargeCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        cast(2, creature.getId());

        harness.assertNotOnBattlefield(player2, "Craw Wurm");
        harness.assertInGraveyard(player2, "Craw Wurm");
    }

    @Test
    @DisplayName("Magic cannot target a creature with power less than 4")
    void magicRejectsSmallCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCard();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Item makes its controller gain 4 life")
    void itemGainsLife() {
        harness.setLife(player1, 10);
        cast(3, null);

        harness.assertLife(player1, 14);
    }

    private void cast(int mode, java.util.UUID targetId) {
        prepareCard();
        harness.castInstant(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new BattleMenu()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
