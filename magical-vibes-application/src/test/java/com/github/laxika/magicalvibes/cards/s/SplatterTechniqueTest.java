package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SplatterTechniqueTest extends BaseCardTest {

    private void addManaFor(int extraRed) {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2 + extraRed);
    }

    

    @Nested
    @DisplayName("Mode 0: Draw four cards")
    class DrawMode {

        @Test
        @DisplayName("Controller draws four cards")
        void drawsFourCards() {
            harness.setHand(player1, List.of(new SplatterTechnique()));
            harness.setLibrary(player1, List.of(
                    new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                    new GrizzlyBears(), new GrizzlyBears()));
            addManaFor(1);

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        }
    }

    @Nested
    @DisplayName("Mode 1: 4 damage to each creature and planeswalker")
    class MassDamageMode {

        @Test
        @DisplayName("Kills creatures with toughness 4 or less on both sides")
        void killsSmallCreatures() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new SplatterTechnique()));
            addManaFor(1);

            harness.castSorcery(player1, 0, 1);
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Does not kill creatures with toughness greater than 4")
        void doesNotKillLargeCreatures() {
            harness.addToBattlefield(player2, new EnormousBaloth());
            harness.setHand(player1, List.of(new SplatterTechnique()));
            addManaFor(1);

            harness.castSorcery(player1, 0, 1);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Enormous Baloth"));
        }
    }
}
