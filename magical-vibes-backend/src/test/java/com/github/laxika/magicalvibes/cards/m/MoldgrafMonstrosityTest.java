package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoldgrafMonstrosityTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_DEATH effect that exiles self and returns two random creatures to battlefield")
    void hasCorrectEffect() {
        MoldgrafMonstrosity card = new MoldgrafMonstrosity();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst())
                .isInstanceOf(ReturnCardFromGraveyardEffect.class);

        ReturnCardFromGraveyardEffect effect =
                (ReturnCardFromGraveyardEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(effect.destination()).isEqualTo(GraveyardChoiceDestination.BATTLEFIELD);
        assertThat(effect.filter()).isInstanceOf(CardTypePredicate.class);
        assertThat(((CardTypePredicate) effect.filter()).cardType()).isEqualTo(CardType.CREATURE);
        assertThat(effect.returnAtRandom()).isTrue();
        assertThat(effect.randomCount()).isEqualTo(2);
        assertThat(effect.exileSourceFromGraveyard()).isTrue();
    }

    // ===== Death trigger =====

    @Nested
    @DisplayName("Death trigger")
    class DeathTriggerTests {

        @Test
        @DisplayName("When Moldgraf Monstrosity dies, it is exiled and two creature cards are returned from graveyard to battlefield")
        void deathTriggerExilesSelfAndReturnsTwoCreatures() {
            harness.addToBattlefield(player1, new MoldgrafMonstrosity());
            harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves()));

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath — Moldgraf Monstrosity dies

            // Death trigger should be on the stack
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

            // Resolve the death trigger
            harness.passBothPriorities();

            // Moldgraf Monstrosity should be exiled, not in graveyard
            harness.assertNotInGraveyard(player1, "Moldgraf Monstrosity");
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Moldgraf Monstrosity"));

            // Both creatures should be on the battlefield
            List<Permanent> creatures = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears")
                            || p.getCard().getName().equals("Llanowar Elves"))
                    .toList();
            assertThat(creatures).hasSize(2);

            // Graveyard should only have Wrath of God
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"))
                    .noneMatch(c -> c.getName().equals("Llanowar Elves"));
        }

        @Test
        @DisplayName("Moldgraf Monstrosity cannot return itself from graveyard")
        void cannotReturnItself() {
            harness.addToBattlefield(player1, new MoldgrafMonstrosity());
            // No other creatures in graveyard — only Moldgraf itself will be there after dying

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath
            harness.passBothPriorities(); // Resolve death trigger

            // Moldgraf Monstrosity should be exiled
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Moldgraf Monstrosity"));

            // No creatures on battlefield (it was exiled, so it can't return itself)
            assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        }

        @Test
        @DisplayName("Returns only one creature when graveyard has only one creature card")
        void returnsOneCreatureWhenOnlyOneAvailable() {
            harness.addToBattlefield(player1, new MoldgrafMonstrosity());
            harness.setGraveyard(player1, List.of(new GrizzlyBears()));

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath
            harness.passBothPriorities(); // Resolve death trigger

            // Moldgraf Monstrosity should be exiled
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Moldgraf Monstrosity"));

            // Grizzly Bears should be on the battlefield
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Only returns creature cards, not non-creature cards")
        void onlyReturnsCreatureCards() {
            harness.addToBattlefield(player1, new MoldgrafMonstrosity());
            // Put a non-creature (Wrath of God) and a creature (Grizzly Bears) in graveyard
            harness.setGraveyard(player1, List.of(new WrathOfGod(), new GrizzlyBears()));

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath
            harness.passBothPriorities(); // Resolve death trigger

            // Grizzly Bears should be on the battlefield
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

            // Wrath of God (non-creature) should still be in graveyard
            harness.assertInGraveyard(player1, "Wrath of God");
        }

        @Test
        @DisplayName("Returns two out of three when graveyard has three creature cards")
        void returnsTwoOutOfThreeCreatures() {
            harness.addToBattlefield(player1, new MoldgrafMonstrosity());
            harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves(), new GrizzlyBears()));

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath
            harness.passBothPriorities(); // Resolve death trigger

            // Two creatures should be on the battlefield
            long creaturesOnBattlefield = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears")
                            || p.getCard().getName().equals("Llanowar Elves"))
                    .count();
            assertThat(creaturesOnBattlefield).isEqualTo(2);

            // One creature should remain in graveyard
            long creaturesInGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                    .filter(c -> c.getName().equals("Grizzly Bears")
                            || c.getName().equals("Llanowar Elves"))
                    .count();
            assertThat(creaturesInGraveyard).isEqualTo(1);
        }

        @Test
        @DisplayName("Does nothing when graveyard has no creature cards after exile")
        void doesNothingWithNoCreaturesAfterExile() {
            harness.addToBattlefield(player1, new MoldgrafMonstrosity());
            // Empty graveyard — only Moldgraf Monstrosity itself will be there after dying

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath
            harness.passBothPriorities(); // Resolve death trigger

            // Moldgraf Monstrosity should be exiled
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Moldgraf Monstrosity"));

            // No creatures on battlefield
            assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        }
    }
}
