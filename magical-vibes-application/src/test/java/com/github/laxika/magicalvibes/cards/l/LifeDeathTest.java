package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LifeDeath.class, GaeasSkyfolk.class, YavimayaCoast.class})
class LifeDeathTest extends BaseCardTest {

    @Test
    void lifeAnimatesOnlyYourLandsAsOneOneCreaturesUntilEndOfTurn() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new YavimayaCoast());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new YavimayaCoast());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new YavimayaCoast());
        Permanent skyfolk = harness.addToBattlefieldAndReturn(player1, new GaeasSkyfolk());

        harness.setHand(player1, List.of(new LifeDeath()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        for (Permanent land : List.of(firstLand, secondLand)) {
            assertThat(gqs.isLand(gd, land)).isTrue();
            assertThat(gqs.isCreature(gd, land)).isTrue();
            assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(1);
        }
        assertThat(gqs.isLand(gd, opposingLand)).isTrue();
        assertThat(gqs.isCreature(gd, opposingLand)).isFalse();
        assertThat(gqs.isCreature(gd, skyfolk)).isTrue();
        assertThat(gqs.getEffectivePower(gd, skyfolk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skyfolk)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        for (Permanent land : List.of(firstLand, secondLand)) {
            assertThat(gqs.isLand(gd, land)).isTrue();
            assertThat(gqs.isCreature(gd, land)).isFalse();
        }
    }

    @Test
    void deathReturnsYourTargetCreatureAndLosesItsManaValue() {
        Card creature = new GaeasSkyfolk();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new LifeDeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
        harness.assertLife(player1, 18);
    }

    @Test
    void deathCannotTargetANonCreatureCard() {
        Card land = new YavimayaCoast();
        harness.setGraveyard(player1, List.of(land));
        harness.setHand(player1, List.of(new LifeDeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deathCannotTargetACreatureInAnOpponentsGraveyard() {
        Card creature = new GaeasSkyfolk();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new LifeDeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
