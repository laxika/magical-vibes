package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LifeDeath.class, Forest.class, Mountain.class, GrizzlyBears.class})
class LifeDeathTest extends BaseCardTest {

    @Test
    void lifeAnimatesOnlyYourLandsAsOneOneCreaturesUntilEndOfTurn() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent opposingForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new LifeDeath()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        for (Permanent land : List.of(forest, mountain)) {
            assertThat(gqs.isLand(gd, land)).isTrue();
            assertThat(gqs.isCreature(gd, land)).isTrue();
            assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(1);
        }
        assertThat(gqs.isCreature(gd, opposingForest)).isFalse();
        assertThat(gqs.isCreature(gd, bears)).isTrue();

        forest.resetModifiers();
        mountain.resetModifiers();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }

    @Test
    void deathReturnsYourTargetCreatureAndLosesItsManaValue() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new LifeDeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 1, creature.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
        harness.assertLife(player1, 18);
    }

    @Test
    void deathCannotTargetANonCreatureCard() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setHand(player1, List.of(new LifeDeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> {
            harness.ensurePriority(player1);
            gs.playCard(gd, player1, 0, 1, land.getId(), null);
        }).isInstanceOf(IllegalStateException.class);
    }
}
