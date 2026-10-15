package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RocEgg.class, WrathOfGod.class, LightningBolt.class, Unsummon.class})
class RocEggTest extends BaseCardTest {

    @Nested
    @DisplayName("Death trigger")
    @CardUsed({RocEgg.class, WrathOfGod.class, LightningBolt.class, Unsummon.class})
    class DeathTriggerTests {

        @Test
        @DisplayName("When Roc Egg dies, a 3/3 white Bird token with flying is created")
        void deathTriggerCreatesBirdToken() {
            harness.addToBattlefield(player1, new RocEgg());

            harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
            harness.passBothPriorities(); // Resolve Wrath — Roc Egg dies

            // Roc Egg should be in the graveyard
            harness.assertInGraveyard(player1, "Roc Egg");

            // Death trigger should be on the stack
            assertThat(gd.stack).hasSize(1);

            // Resolve the death trigger
            harness.passBothPriorities();

            // A 3/3 Bird token with flying should be on the battlefield
            List<Permanent> tokens = findPermanents(player1, "Bird");
            assertThat(tokens).hasSize(1);

            Permanent birdToken = tokens.getFirst();
            assertThat(birdToken.getCard().getPower()).isEqualTo(3);
            assertThat(birdToken.getCard().getToughness()).isEqualTo(3);
            assertThat(birdToken.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(birdToken.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(birdToken.getCard().getSubtypes()).contains(CardSubtype.BIRD);
            assertThat(birdToken.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(birdToken.getCard().isToken()).isTrue();
        }

        @Test
        @DisplayName("Roc Egg death trigger belongs to its controller")
        void deathTriggerBelongsToController() {
            harness.addToBattlefield(player2, new RocEgg());

            harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
            harness.passBothPriorities(); // Resolve Wrath — Roc Egg dies
            harness.passBothPriorities(); // Resolve death trigger

            // The Bird token should be on player2's battlefield (the Roc Egg's controller)
            List<Permanent> player2Tokens = findPermanents(player2, "Bird");
            assertThat(player2Tokens).hasSize(1);

            // Player1 should have no Bird tokens
            List<Permanent> player1Tokens = findPermanents(player1, "Bird");
            assertThat(player1Tokens).isEmpty();
        }

        @Test
        void lethalDamageCreatesTokenOnlyAfterTriggerResolves() {
            Permanent egg = harness.addToBattlefieldAndReturn(player1, new RocEgg());
            harness.setHand(player1, List.of(new LightningBolt()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castAndResolveInstant(player1, 0, egg.getId());

            harness.assertInGraveyard(player1, "Roc Egg");
            assertThat(gd.stack).hasSize(1);
            assertThat(findPermanents(player1, "Bird")).isEmpty();

            resolveAllTriggers();

            assertThat(findPermanents(player1, "Bird")).hasSize(1);
            assertThat(findPermanent(player1, "Bird").isTapped()).isFalse();
        }

        @Test
        void returningEggToHandDoesNotCreateToken() {
            Permanent egg = harness.addToBattlefieldAndReturn(player1, new RocEgg());
            harness.setHand(player1, List.of(new Unsummon()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.castAndResolveInstant(player1, 0, egg.getId());

            harness.assertInHand(player1, "Roc Egg");
            harness.assertNotInGraveyard(player1, "Roc Egg");
            assertThat(gd.stack).isEmpty();
            assertThat(findPermanents(player1, "Bird")).isEmpty();
        }

        @Test
        void eachEggCreatesItsOwnTokenWhenTheyDieTogether() {
            harness.addToBattlefield(player1, new RocEgg());
            harness.addToBattlefield(player1, new RocEgg());
            harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");

            harness.passBothPriorities();
            assertThat(gd.stack).hasSize(2);
            assertThat(findPermanents(player1, "Bird")).isEmpty();

            resolveAllTriggers();

            assertThat(findPermanents(player1, "Bird")).hasSize(2);
            harness.assertNotOnBattlefield(player1, "Roc Egg");
        }
    }

    @Test
    void defenderPreventsEggFromAttacking() {
        addCreatureReady(player1, new RocEgg());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
