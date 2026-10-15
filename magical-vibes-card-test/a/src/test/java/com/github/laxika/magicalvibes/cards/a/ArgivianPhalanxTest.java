package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArgivianPhalanx.class, GrizzlyBears.class, Spellbook.class})
class ArgivianPhalanxTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for creatures reduces the generic mana cost")
    void affinityForCreaturesReducesGenericCost() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        harness.setHand(player1, List.of(new ArgivianPhalanx()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only creatures controlled by the spell's controller")
    void affinityCountsOnlyControlledCreatures() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Spellbook());
        }
        harness.setHand(player1, List.of(new ArgivianPhalanx()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Vigilance keeps Argivian Phalanx untapped after attacking")
    void vigilanceDoesNotTapOnAttack() {
        Permanent phalanx = addCreatureReady(player1, new ArgivianPhalanx());

        declareAttackers(List.of(0));

        assertThat(phalanx.isTapped()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 2, 7})
    @DisplayName("Affinity counts tapped creatures and reduces only the generic cost")
    void affinityCountsTappedCreaturesAndStopsAtZeroGenericCost(int creatureCount) {
        for (int i = 0; i < creatureCount; i++) {
            harness.addToBattlefieldAndReturn(player1, new ArgivianPhalanx()).tap();
        }
        harness.setHand(player1, List.of(new ArgivianPhalanx()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, Math.max(0, 5 - creatureCount));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity cannot pay the white mana requirement even with excess creatures")
    void affinityDoesNotReduceWhiteManaCost() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new ArgivianPhalanx());
        }
        harness.setHand(player1, List.of(new ArgivianPhalanx()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
