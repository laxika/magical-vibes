package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HyenaUmbra;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrzhovCharm.class, GrizzlyBears.class, GiantSpider.class, Pacifism.class, SuntailHawk.class,
        HyenaUmbra.class, Ornithopter.class})
class OrzhovCharmTest extends BaseCardTest {

    // Mode indices: 0 = bounce your creature + your Auras on it, 1 = destroy target creature and
    //               lose life equal to its toughness, 2 = reanimate a mana value 1 or less creature.

    private void addWB() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    @Nested
    @DisplayName("Mode 0: Return target creature you control and your Auras attached to it")
    @CardUsed({OrzhovCharm.class, GrizzlyBears.class, Pacifism.class, HyenaUmbra.class})
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
            harness.assertNotInHand(player2, "Pacifism");
            harness.assertInGraveyard(player2, "Pacifism");
        }

        @Test
        @DisplayName("Bouncing a creature returns its Umbra without destroying either permanent")
        void returnsCreatureAndUmbra() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            Permanent umbra = harness.addToBattlefieldAndReturn(player1, new HyenaUmbra());
            umbra.setAttachedTo(bears.getId());
            harness.setHand(player1, List.of(new OrzhovCharm()));
            addWB();

            harness.castInstant(player1, 0, 0, bears.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertNotOnBattlefield(player1, "Hyena Umbra");
            harness.assertInHand(player1, "Grizzly Bears");
            harness.assertInHand(player1, "Hyena Umbra");
            harness.assertNotInGraveyard(player1, "Hyena Umbra");
        }

        @Test
        @DisplayName("Returns every controlled Aura and the creature to their respective owners")
        void returnsAllControlledAurasToTheirOwners() {
            Card creatureCard = new GrizzlyBears();
            creatureCard.setOwnerId(player2.getId());
            Permanent bears = harness.addToBattlefieldAndReturn(player1, creatureCard);
            Card auraCard = new Pacifism();
            auraCard.setOwnerId(player2.getId());
            Permanent pacifism = harness.addToBattlefieldAndReturn(player1, auraCard);
            pacifism.setAttachedTo(bears.getId());
            Permanent umbra = harness.addToBattlefieldAndReturn(player1, new HyenaUmbra());
            umbra.setAttachedTo(bears.getId());
            harness.setHand(player1, List.of(new OrzhovCharm()));
            addWB();

            harness.castInstant(player1, 0, 0, bears.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertNotOnBattlefield(player1, "Pacifism");
            harness.assertNotOnBattlefield(player1, "Hyena Umbra");
            harness.assertInHand(player2, "Grizzly Bears");
            harness.assertInHand(player2, "Pacifism");
            harness.assertInHand(player1, "Hyena Umbra");
            harness.assertNotInHand(player1, "Grizzly Bears");
            harness.assertNotInHand(player1, "Pacifism");
            harness.assertNotInHand(player2, "Hyena Umbra");
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
    @CardUsed({OrzhovCharm.class, GiantSpider.class, GrizzlyBears.class, Pacifism.class, HyenaUmbra.class})
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
        @DisplayName("Uses the surviving creature's toughness after Umbra armor removes its bonus")
        void losesLifeAfterUmbraArmorReplacement() {
            harness.setLife(player1, 20);
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            Permanent umbra = harness.addToBattlefieldAndReturn(player2, new HyenaUmbra());
            umbra.setAttachedTo(bears.getId());
            harness.setHand(player1, List.of(new OrzhovCharm()));
            addWB();

            harness.castInstant(player1, 0, 1, bears.getId());
            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, "Grizzly Bears");
            harness.assertNotOnBattlefield(player2, "Hyena Umbra");
            harness.assertInGraveyard(player2, "Hyena Umbra");
            harness.assertLife(player1, 18);
        }

        @Test
        @DisplayName("Uses toughness with counters when destroying a creature you control")
        void destroysOwnCreatureWithModifiedToughness() {
            harness.setLife(player1, 20);
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
            harness.setHand(player1, List.of(new OrzhovCharm()));
            addWB();

            harness.castInstant(player1, 0, 1, bears.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertInGraveyard(player1, "Grizzly Bears");
            harness.assertLife(player1, 16);
        }

        @Test
        @DisplayName("Still loses life when the creature regenerates")
        void losesLifeWhenCreatureRegenerates() {
            harness.setLife(player1, 20);
            Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
            spider.setRegenerationShield(1);
            harness.setHand(player1, List.of(new OrzhovCharm()));
            addWB();

            harness.castInstant(player1, 0, 1, spider.getId());
            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, "Giant Spider");
            harness.assertNotInGraveyard(player2, "Giant Spider");
            harness.assertLife(player1, 16);
        }

        @Test
        @DisplayName("Does not lose life if the creature is returned to hand in response")
        void noLifeLossWhenTargetLeavesBattlefield() {
            harness.setLife(player1, 20);
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new OrzhovCharm()));
            addWB();
            harness.castInstant(player1, 0, 1, bears.getId());

            harness.setHand(player2, List.of(new OrzhovCharm()));
            harness.addMana(player2, ManaColor.WHITE, 1);
            harness.addMana(player2, ManaColor.BLACK, 1);
            harness.castInstant(player2, 0, 0, bears.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.assertInHand(player2, "Grizzly Bears");
            harness.assertNotInGraveyard(player2, "Grizzly Bears");
            harness.assertLife(player1, 20);
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent")
        void cannotTargetNoncreature() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
            aura.setAttachedTo(bears.getId());
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
    @CardUsed({OrzhovCharm.class, SuntailHawk.class, GrizzlyBears.class, HyenaUmbra.class, Ornithopter.class})
    class ReanimateMode {

        @Test
        @DisplayName("Returns a mana value 1 creature from your graveyard to the battlefield")
        void reanimatesCheapCreature() {
            Card hawk = new SuntailHawk();
            harness.setGraveyard(player1, List.of(hawk));
            harness.setHand(player1, List.of(new OrzhovCharm()));
            addWB();

            harness.castInstant(player1, 0, 2, hawk.getId());
            harness.passBothPriorities();

            harness.assertNotInGraveyard(player1, "Suntail Hawk");
            harness.assertOnBattlefield(player1, "Suntail Hawk");
        }

        @Test
        @DisplayName("Returns a mana value zero artifact creature")
        void reanimatesZeroManaCreature() {
            Card thopter = new Ornithopter();
            harness.setGraveyard(player1, List.of(thopter));
            harness.setHand(player1, List.of(new OrzhovCharm()));
            addWB();

            harness.castInstant(player1, 0, 2, thopter.getId());
            harness.passBothPriorities();

            harness.assertNotInGraveyard(player1, "Ornithopter");
            harness.assertOnBattlefield(player1, "Ornithopter");
        }

        @Test
        @DisplayName("Cannot target a cheap creature in an opponent's graveyard")
        void cannotReanimateOpponentCreature() {
            Card hawk = new SuntailHawk();
            harness.setGraveyard(player2, List.of(hawk));
            harness.setHand(player1, List.of(new OrzhovCharm()));
            addWB();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, hawk.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("your graveyard");
        }

        @Test
        @DisplayName("Cannot reanimate a noncreature card even when its mana value is one")
        void cannotReanimateCheapAura() {
            Card aura = new HyenaUmbra();
            harness.setGraveyard(player1, List.of(aura));
            harness.setHand(player1, List.of(new OrzhovCharm()));
            addWB();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, aura.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot target a creature card with mana value 2")
        void cannotTargetExpensiveCreature() {
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(bears));
            harness.setHand(player1, List.of(new OrzhovCharm()));
            addWB();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
