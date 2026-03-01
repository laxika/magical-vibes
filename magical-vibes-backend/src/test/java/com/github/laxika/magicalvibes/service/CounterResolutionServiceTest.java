package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GaeasHerald;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.s.SpiketailHatchling;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CounterResolutionServiceTest extends BaseCardTest {

    // =========================================================================
    // resolveCounterSpell (CounterSpellEffect)
    // =========================================================================

    @Nested
    @DisplayName("resolveCounterSpell")
    class ResolveCounterSpell {

        @Test
        @DisplayName("Counters a creature spell and puts it in the graveyard")
        void countersCreatureSpellAndPutsInGraveyard() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.setHand(player2, List.of(new Cancel()));
            harness.addMana(player2, ManaColor.BLUE, 3);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, bears.getId());
            harness.passBothPriorities();

            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Counters an instant spell and puts it in the graveyard")
        void countersInstantSpellAndPutsInGraveyard() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.addToBattlefield(player1, bears);

            MightOfOaks might = new MightOfOaks();
            harness.setHand(player1, List.of(might));
            harness.addMana(player1, ManaColor.GREEN, 4);

            harness.setHand(player2, List.of(new Cancel()));
            harness.addMana(player2, ManaColor.BLUE, 3);

            harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
            harness.passPriority(player1);
            harness.castInstant(player2, 0, might.getId());
            harness.passBothPriorities();

            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Might of Oaks"));
            assertThat(gd.stack)
                    .noneMatch(se -> se.getCard().getName().equals("Might of Oaks"));
        }

        @Test
        @DisplayName("Does nothing when target spell is no longer on the stack")
        void doesNothingWhenTargetNoLongerOnStack() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.setHand(player2, List.of(new Cancel()));
            harness.addMana(player2, ManaColor.BLUE, 3);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, bears.getId());

            // Remove the target spell before Cancel resolves
            gd.stack.removeIf(se -> se.getCard().getName().equals("Grizzly Bears"));

            harness.passBothPriorities();

            // Bears should not be in graveyard (was removed from stack externally)
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        }

        @Test
        @DisplayName("Does not counter an uncounterable creature spell (Gaea's Herald on battlefield)")
        void doesNotCounterUncounterableCreatureSpell() {
            // Gaea's Herald makes creature spells uncounterable
            harness.addToBattlefield(player1, new GaeasHerald());

            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.setHand(player2, List.of(new Cancel()));
            harness.addMana(player2, ManaColor.BLUE, 3);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, bears.getId());
            harness.passBothPriorities();

            // Bears should NOT be countered — it should resolve to the battlefield
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));

            // Resolve the remaining Bears spell on the stack
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Countered spell is logged in the game log")
        void counteredSpellIsLogged() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.setHand(player2, List.of(new Cancel()));
            harness.addMana(player2, ManaColor.BLUE, 3);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, bears.getId());
            harness.passBothPriorities();

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Grizzly Bears") && log.contains("countered"));
        }

        @Test
        @DisplayName("Countered copy ceases to exist and does not go to the graveyard")
        void counteredCopyDoesNotGoToGraveyard() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.setHand(player2, List.of(new Cancel()));
            harness.addMana(player2, ManaColor.BLUE, 3);

            harness.castCreature(player1, 0);

            // Mark the Bears spell on the stack as a copy
            StackEntry bearsEntry = gd.stack.stream()
                    .filter(se -> se.getCard().getName().equals("Grizzly Bears"))
                    .findFirst().orElseThrow();
            bearsEntry.setCopy(true);

            harness.passPriority(player1);
            harness.castInstant(player2, 0, bears.getId());
            harness.passBothPriorities();

            // Copy should NOT go to the graveyard per rule 707.10a
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.stack)
                    .noneMatch(se -> se.getCard().getName().equals("Grizzly Bears"));
        }
    }

    // =========================================================================
    // resolveCounterUnlessPays (CounterUnlessPaysEffect)
    // =========================================================================

    @Nested
    @DisplayName("resolveCounterUnlessPays")
    class ResolveCounterUnlessPays {

        @Test
        @DisplayName("Counters spell immediately when opponent cannot pay")
        void countersImmediatelyWhenCannotPay() {
            SpiketailHatchling hatchling = new SpiketailHatchling();
            harness.addToBattlefield(player2, hatchling);

            LlanowarElves elves = new LlanowarElves();
            harness.setHand(player1, List.of(elves));
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.activateAbility(player2, 0, null, elves.getId());
            harness.passBothPriorities();

            // Elves should be countered — in graveyard, not on battlefield
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Llanowar Elves"));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Logs counter message when spell is countered immediately")
        void logsCounterMessageWhenCannotPay() {
            SpiketailHatchling hatchling = new SpiketailHatchling();
            harness.addToBattlefield(player2, hatchling);

            LlanowarElves elves = new LlanowarElves();
            harness.setHand(player1, List.of(elves));
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.activateAbility(player2, 0, null, elves.getId());
            harness.passBothPriorities();

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Llanowar Elves") && log.contains("countered"));
        }

        @Test
        @DisplayName("Presents may ability choice when opponent can pay")
        void presentsMayAbilityChoiceWhenCanPay() {
            SpiketailHatchling hatchling = new SpiketailHatchling();
            harness.addToBattlefield(player2, hatchling);

            LlanowarElves elves = new LlanowarElves();
            harness.setHand(player1, List.of(elves));
            harness.addMana(player1, ManaColor.GREEN, 2); // 1 to cast, 1 to pay

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.activateAbility(player2, 0, null, elves.getId());
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
            assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Spell resolves when opponent accepts to pay")
        void spellResolvesWhenOpponentPays() {
            SpiketailHatchling hatchling = new SpiketailHatchling();
            harness.addToBattlefield(player2, hatchling);

            LlanowarElves elves = new LlanowarElves();
            harness.setHand(player1, List.of(elves));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.activateAbility(player2, 0, null, elves.getId());
            harness.passBothPriorities();

            // Accept the payment
            harness.handleMayAbilityChosen(player1, true);

            // Elves should NOT be countered
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Llanowar Elves"));

            // Resolve the remaining spell
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        }

        @Test
        @DisplayName("Spell is countered when opponent declines to pay")
        void spellCounteredWhenOpponentDeclines() {
            SpiketailHatchling hatchling = new SpiketailHatchling();
            harness.addToBattlefield(player2, hatchling);

            LlanowarElves elves = new LlanowarElves();
            harness.setHand(player1, List.of(elves));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.activateAbility(player2, 0, null, elves.getId());
            harness.passBothPriorities();

            // Decline the payment
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Llanowar Elves"));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        }

        @Test
        @DisplayName("Opponent's mana pool is reduced after paying")
        void manaPoolReducedAfterPaying() {
            SpiketailHatchling hatchling = new SpiketailHatchling();
            harness.addToBattlefield(player2, hatchling);

            LlanowarElves elves = new LlanowarElves();
            harness.setHand(player1, List.of(elves));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.activateAbility(player2, 0, null, elves.getId());
            harness.passBothPriorities();

            int manaBefore = gd.playerManaPools.get(player1.getId()).getTotal();
            assertThat(manaBefore).isEqualTo(1);

            harness.handleMayAbilityChosen(player1, true);

            int manaAfter = gd.playerManaPools.get(player1.getId()).getTotal();
            assertThat(manaAfter).isEqualTo(0);
        }

        @Test
        @DisplayName("Does nothing when target spell is no longer on the stack")
        void doesNothingWhenTargetNoLongerOnStack() {
            SpiketailHatchling hatchling = new SpiketailHatchling();
            harness.addToBattlefield(player2, hatchling);

            LlanowarElves elves = new LlanowarElves();
            harness.setHand(player1, List.of(elves));
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.activateAbility(player2, 0, null, elves.getId());

            // Remove target spell before ability resolves
            gd.stack.removeIf(se -> se.getCard().getName().equals("Llanowar Elves"));

            harness.passBothPriorities();

            assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Does not counter an uncounterable creature spell (Gaea's Herald on battlefield)")
        void doesNotCounterUncounterableCreatureSpell() {
            // Gaea's Herald makes creature spells uncounterable
            harness.addToBattlefield(player1, new GaeasHerald());

            SpiketailHatchling hatchling = new SpiketailHatchling();
            harness.addToBattlefield(player2, hatchling);

            LlanowarElves elves = new LlanowarElves();
            harness.setHand(player1, List.of(elves));
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);
            harness.activateAbility(player2, 0, null, elves.getId());
            harness.passBothPriorities();

            // Elves should NOT be countered
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Llanowar Elves"));

            // Resolve the remaining spell
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        }

        @Test
        @DisplayName("Countered copy does not go to the graveyard")
        void counteredCopyDoesNotGoToGraveyard() {
            SpiketailHatchling hatchling = new SpiketailHatchling();
            harness.addToBattlefield(player2, hatchling);

            LlanowarElves elves = new LlanowarElves();
            harness.setHand(player1, List.of(elves));
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.castCreature(player1, 0);

            // Mark the Elves spell on the stack as a copy
            StackEntry elvesEntry = gd.stack.stream()
                    .filter(se -> se.getCard().getName().equals("Llanowar Elves"))
                    .findFirst().orElseThrow();
            elvesEntry.setCopy(true);

            harness.passPriority(player1);
            harness.activateAbility(player2, 0, null, elves.getId());
            harness.passBothPriorities();

            // Copy should NOT go to the graveyard per rule 707.10a
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Llanowar Elves"));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
            assertThat(gd.stack)
                    .noneMatch(se -> se.getCard().getName().equals("Llanowar Elves"));
        }
    }
}
