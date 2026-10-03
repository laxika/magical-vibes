package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CoralCommando;
import com.github.laxika.magicalvibes.cards.s.ScreamingShield;
import com.github.laxika.magicalvibes.cards.w.WallOfLostThoughts;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Deface.class, ScreamingShield.class, CoralCommando.class, WallOfLostThoughts.class})
class DefaceTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Destroy target artifact")
    @CardUsed({Deface.class, ScreamingShield.class, CoralCommando.class, WallOfLostThoughts.class})
    class DestroyArtifactMode {

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            Permanent shield = harness.addToBattlefieldAndReturn(player2, new ScreamingShield());
            harness.setHand(player1, List.of(new Deface()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castSorcery(player1, 0, 0, shield.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Screaming Shield");
            harness.assertInGraveyard(player2, "Screaming Shield");
        }

        @Test
        @DisplayName("Cannot target a non-artifact creature")
        void cannotTargetCreature() {
            Permanent commando = harness.addToBattlefieldAndReturn(player2, new CoralCommando());
            harness.setHand(player1, List.of(new Deface()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, commando.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void canDestroyOwnArtifact() {
            Permanent shield = harness.addToBattlefieldAndReturn(player1, new ScreamingShield());
            harness.setHand(player1, List.of(new Deface()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castSorcery(player1, 0, 0, shield.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Screaming Shield");
            harness.assertInGraveyard(player1, "Screaming Shield");
        }

        @Test
        void artifactModeCannotTargetNonartifactDefender() {
            Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfLostThoughts());
            harness.setHand(player1, List.of(new Deface()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, wall.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target creature with defender")
    @CardUsed({Deface.class, WallOfLostThoughts.class, CoralCommando.class, ScreamingShield.class})
    class DestroyDefenderMode {

        @Test
        @DisplayName("Destroys target creature with defender")
        void destroysDefender() {
            Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfLostThoughts());
            harness.setHand(player1, List.of(new Deface()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castSorcery(player1, 0, 1, wall.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Wall of Lost Thoughts");
            harness.assertInGraveyard(player2, "Wall of Lost Thoughts");
        }

        @Test
        @DisplayName("Cannot target a creature without defender")
        void cannotTargetCreatureWithoutDefender() {
            Permanent commando = harness.addToBattlefieldAndReturn(player2, new CoralCommando());
            harness.setHand(player1, List.of(new Deface()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, commando.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void defenderModeCannotTargetNoncreatureArtifact() {
            Permanent shield = harness.addToBattlefieldAndReturn(player2, new ScreamingShield());
            harness.setHand(player1, List.of(new Deface()));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, shield.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void destroysCreatureWithGrantedDefender() {
            Permanent commando = harness.addToBattlefieldAndReturn(player2, new CoralCommando());
            commando.getPersistentGrantedKeywords().add(Keyword.DEFENDER);
            harness.setHand(player1, List.of(new Deface()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castSorcery(player1, 0, 1, commando.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Coral Commando");
            harness.assertInGraveyard(player2, "Coral Commando");
        }

        @Test
        void doesNotDestroyCreatureThatLosesDefenderBeforeResolution() {
            Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfLostThoughts());
            harness.setHand(player1, List.of(new Deface()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castSorcery(player1, 0, 1, wall.getId());
            wall.getRemovedKeywords().add(Keyword.DEFENDER);
            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, "Wall of Lost Thoughts");
            harness.assertNotInGraveyard(player2, "Wall of Lost Thoughts");
            harness.assertInGraveyard(player1, "Deface");
        }
    }
}
