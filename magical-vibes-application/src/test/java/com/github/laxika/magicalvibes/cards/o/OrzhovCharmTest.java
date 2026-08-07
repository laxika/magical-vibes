package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrzhovCharmTest extends BaseCardTest {

    // Mode indices: 0 = bounce your creature + your Auras on it, 1 = destroy target creature and
    //               lose life equal to its toughness, 2 = reanimate a mana value 1 or less creature.

    private void addWB() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    @Nested
    @DisplayName("Mode 0: Return target creature you control and your Auras attached to it")
    class BounceMode {

        @Test
        @DisplayName("Returns the creature and the controller's Aura to their owners' hands")
        void returnsCreatureAndOwnAura() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
            aura.setAttachedTo(bears.getId());

            harness.setHand(player1, List.of(new OrzhovCharm()));
            addWB();

            harness.castInstant(player1, 0, 0, bears.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertNotOnBattlefield(player1, "Pacifism");
            harness.assertInHand(player1, "Grizzly Bears");
            harness.assertInHand(player1, "Pacifism");
        }

        @Test
        @DisplayName("Does not bounce an opponent's Aura — it falls off into their graveyard")
        void doesNotBounceOpponentAura() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            Permanent opponentAura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
            opponentAura.setAttachedTo(bears.getId());

            harness.setHand(player1, List.of(new OrzhovCharm()));
            addWB();

            harness.castInstant(player1, 0, 0, bears.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            assertThat(gd.playerHands.get(player2.getId()))
                    .doesNotContain(opponentAura.getOriginalCard());
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .contains(opponentAura.getOriginalCard());
        }

        @Test
        @DisplayName("Cannot bounce a creature an opponent controls")
        void cannotTargetOpponentCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new OrzhovCharm()));
            addWB();

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target creature and lose life equal to its toughness")
    class DestroyMode {

        @Test
        @DisplayName("Destroys the creature and the caster loses life equal to its toughness")
        void destroysAndLosesLife() {
            harness.setLife(player1, 20);
            harness.addToBattlefield(player2, new GiantSpider());
            harness.setHand(player1, List.of(new OrzhovCharm()));
            addWB();

            harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Giant Spider"));
            harness.passBothPriorities();

            // Giant Spider is a 2/4 → the caster loses 4 life (20 - 4 = 16)
            harness.assertNotOnBattlefield(player2, "Giant Spider");
            harness.assertLife(player1, 16);
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent")
        void cannotTargetNoncreature() {
            harness.addToBattlefield(player2, new Pacifism());
            harness.setHand(player1, List.of(new OrzhovCharm()));
            addWB();

            UUID targetId = harness.getPermanentId(player2, "Pacifism");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Target must be a creature");
        }
    }

    @Nested
    @DisplayName("Mode 2: Reanimate a creature card with mana value 1 or less")
    class ReanimateMode {

        @Test
        @DisplayName("Returns a mana value 1 creature from your graveyard to the battlefield")
        void reanimatesCheapCreature() {
            Card hawk = new SuntailHawk();
            harness.setGraveyard(player1, new ArrayList<>(List.of(hawk)));
            harness.setHand(player1, List.of(new OrzhovCharm()));
            addWB();

            harness.castInstant(player1, 0, 2, hawk.getId());
            harness.passBothPriorities();

            harness.assertNotInGraveyard(player1, "Suntail Hawk");
            harness.assertOnBattlefield(player1, "Suntail Hawk");
        }

        @Test
        @DisplayName("Cannot target a creature card with mana value 2")
        void cannotTargetExpensiveCreature() {
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
            harness.setHand(player1, List.of(new OrzhovCharm()));
            addWB();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
