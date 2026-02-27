package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.e.Evacuation;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HurkylsRecall;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.s.ScoriaWurm;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.StampedingWildebeests;
import com.github.laxika.magicalvibes.cards.s.SunkenHope;
import com.github.laxika.magicalvibes.cards.v.ViashinoSandscout;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BounceResolutionServiceTest extends BaseCardTest {


    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    // =========================================================================
    // ReturnSelfToHandEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveReturnSelfToHand")
    class ResolveReturnSelfToHand {

        @Test
        @DisplayName("Returns the permanent to its owner's hand")
        void returnsPermanentToOwnersHand() {
            Permanent sandscout = new Permanent(new ViashinoSandscout());
            gd.playerBattlefields.get(player1.getId()).add(sandscout);

            advanceToEndStep(player1);
            // trigger on stack
            assertThat(gd.stack).hasSize(1);

            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Viashino Sandscout"));
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Viashino Sandscout"));
        }

        @Test
        @DisplayName("Does nothing when the permanent is already removed from the battlefield")
        void doesNothingWhenAlreadyRemoved() {
            Permanent sandscout = new Permanent(new ViashinoSandscout());
            gd.playerBattlefields.get(player1.getId()).add(sandscout);

            advanceToEndStep(player1);
            assertThat(gd.stack).hasSize(1);

            // Remove Viashino Sandscout before resolution (e.g. killed in response)
            gd.playerBattlefields.get(player1.getId()).remove(sandscout);

            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Viashino Sandscout"));
            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("is no longer on the battlefield"));
        }
    }

    // =========================================================================
    // ReturnTargetPermanentToHandEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveReturnTargetPermanentToHand")
    class ResolveReturnTargetPermanentToHand {

        @Test
        @DisplayName("Returns target creature to its owner's hand")
        void returnsTargetCreatureToHand() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new Boomerang()));
            harness.addMana(player1, ManaColor.BLUE, 2);

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castInstant(player1, 0, targetId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerHands.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Fizzles when target is removed before resolution")
        void fizzlesWhenTargetRemoved() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new Boomerang()));
            harness.addMana(player1, ManaColor.BLUE, 2);

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castInstant(player1, 0, targetId);

            // Remove target before resolution
            gd.playerBattlefields.get(player2.getId()).clear();

            harness.passBothPriorities();

            assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        }

        @Test
        @DisplayName("Stolen creature returns to its original owner's hand, not controller's")
        void stolenCreatureReturnsToOriginalOwner() {
            // Place a creature on player1's battlefield, but mark it as owned by player2 (stolen)
            Permanent stolen = addPermanent(player1, new GrizzlyBears());
            gd.stolenCreatures.put(stolen.getId(), player2.getId());

            harness.setHand(player1, List.of(new Boomerang()));
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.castInstant(player1, 0, stolen.getId());
            harness.passBothPriorities();

            // Should be in player2's hand (original owner), not player1's (controller)
            assertThat(gd.playerHands.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerHands.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));

            // stolenCreatures entry should be cleaned up
            assertThat(gd.stolenCreatures).doesNotContainKey(stolen.getId());
        }

        @Test
        @DisplayName("Can bounce own permanent")
        void canBounceOwnPermanent() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new Boomerang()));
            harness.addMana(player1, ManaColor.BLUE, 2);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, targetId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }
    }

    // =========================================================================
    // ReturnCreaturesToOwnersHandEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveReturnCreaturesToOwnersHand")
    class ResolveReturnCreaturesToOwnersHand {

        @Test
        @DisplayName("Returns all creatures on both sides to their owners' hands")
        void returnsAllCreaturesToHands() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new SerraAngel());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new Evacuation()));
            harness.addMana(player1, ManaColor.BLUE, 5);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> gqs.isCreature(gd, p));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> gqs.isCreature(gd, p));

            assertThat(gd.playerHands.get(player1.getId()))
                    .extracting(c -> c.getName())
                    .containsExactlyInAnyOrder("Grizzly Bears", "Serra Angel");
            assertThat(gd.playerHands.get(player2.getId()))
                    .extracting(c -> c.getName())
                    .contains("Grizzly Bears");
        }

        @Test
        @DisplayName("Does not return non-creature permanents")
        void doesNotReturnNonCreatures() {
            harness.addToBattlefield(player1, new GloriousAnthem());
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new Evacuation()));
            harness.addMana(player1, ManaColor.BLUE, 5);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Glorious Anthem"));
            assertThat(gd.playerHands.get(player1.getId()))
                    .extracting(c -> c.getName())
                    .containsExactly("Grizzly Bears");
        }

        @Test
        @DisplayName("Stolen creatures return to their original owner's hand")
        void stolenCreaturesReturnToOriginalOwner() {
            // Place a creature on player1's battlefield, but it's owned by player2
            Permanent stolen = addPermanent(player1, new GrizzlyBears());
            gd.stolenCreatures.put(stolen.getId(), player2.getId());

            // Player1's own creature
            addPermanent(player1, new SerraAngel());

            harness.setHand(player1, List.of(new Evacuation()));
            harness.addMana(player1, ManaColor.BLUE, 5);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            // Stolen Grizzly Bears should go to player2's hand (original owner)
            assertThat(gd.playerHands.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));

            // Serra Angel should go to player1's hand (controller and owner)
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Serra Angel"));

            // stolenCreatures entry should be cleaned up
            assertThat(gd.stolenCreatures).doesNotContainKey(stolen.getId());
        }

        @Test
        @DisplayName("Works with empty battlefields without crashing")
        void worksWithEmptyBattlefields() {
            harness.setHand(player1, List.of(new Evacuation()));
            harness.addMana(player1, ManaColor.BLUE, 5);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
        }
    }

    // =========================================================================
    // ReturnArtifactsTargetPlayerOwnsToHandEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveReturnArtifactsTargetPlayerOwnsToHand")
    class ResolveReturnArtifactsTargetPlayerOwnsToHand {

        @Test
        @DisplayName("Returns all artifacts target player owns to their hand")
        void returnsAllArtifactsToTargetPlayersHand() {
            harness.addToBattlefield(player2, new AngelsFeather());
            harness.addToBattlefield(player2, new IcyManipulator());
            harness.setHand(player1, List.of(new HurkylsRecall()));
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getType() == CardType.ARTIFACT);
            assertThat(gd.playerHands.get(player2.getId()))
                    .extracting(c -> c.getName())
                    .contains("Angel's Feather", "Icy Manipulator");
        }

        @Test
        @DisplayName("Does not return non-artifact permanents")
        void doesNotReturnNonArtifacts() {
            harness.addToBattlefield(player2, new AngelsFeather());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new HurkylsRecall()));
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerHands.get(player2.getId()))
                    .extracting(c -> c.getName())
                    .contains("Angel's Feather");
        }

        @Test
        @DisplayName("Does not affect other player's artifacts")
        void doesNotAffectOtherPlayersArtifacts() {
            harness.addToBattlefield(player1, new AngelsFeather());
            harness.addToBattlefield(player2, new IcyManipulator());
            harness.setHand(player1, List.of(new HurkylsRecall()));
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Angel's Feather"));
            assertThat(gd.playerHands.get(player2.getId()))
                    .extracting(c -> c.getName())
                    .contains("Icy Manipulator");
        }

        @Test
        @DisplayName("No-op when target player has no artifacts")
        void noOpWhenNoArtifacts() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new HurkylsRecall()));
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Can target self to return own artifacts")
        void canTargetSelf() {
            harness.addToBattlefield(player1, new AngelsFeather());
            harness.setHand(player1, List.of(new HurkylsRecall()));
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.castInstant(player1, 0, player1.getId());
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getType() == CardType.ARTIFACT);
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Angel's Feather"));
        }
    }

    // =========================================================================
    // BounceCreatureOnUpkeepEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveBounceCreatureOnUpkeep")
    class ResolveBounceCreatureOnUpkeep {

        @Test
        @DisplayName("TRIGGER_TARGET_PLAYER scope prompts the active player to choose")
        void triggerTargetPlayerScopePromptsActivePlayer() {
            harness.addToBattlefield(player1, new SunkenHope());
            Permanent creature = addPermanent(player2, new GrizzlyBears());

            advanceToUpkeep(player2);
            harness.passBothPriorities(); // resolve trigger

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
            assertThat(gd.interaction.awaitingPermanentChoicePlayerId()).isEqualTo(player2.getId());
            assertThat(gd.interaction.awaitingPermanentChoiceValidIds()).contains(creature.getId());
        }

        @Test
        @DisplayName("SOURCE_CONTROLLER scope prompts the source controller to choose")
        void sourceControllerScopePromptsController() {
            harness.addToBattlefield(player1, new StampedingWildebeests());
            Permanent creature = addPermanent(player1, new GrizzlyBears());

            advanceToUpkeep(player1);
            harness.passBothPriorities(); // resolve trigger

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
            assertThat(gd.interaction.awaitingPermanentChoicePlayerId()).isEqualTo(player1.getId());
            assertThat(gd.interaction.awaitingPermanentChoiceValidIds()).contains(creature.getId());
        }

        @Test
        @DisplayName("Sets BounceCreature permanent choice context")
        void setsBounceCreatureContext() {
            harness.addToBattlefield(player1, new SunkenHope());
            addPermanent(player1, new GrizzlyBears());

            advanceToUpkeep(player1);
            harness.passBothPriorities();

            assertThat(gd.interaction.permanentChoiceContext())
                    .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
        }

        @Test
        @DisplayName("Logs and skips when no valid creatures exist")
        void logsWhenNoValidCreatures() {
            harness.addToBattlefield(player1, new SunkenHope());
            // No creatures on player1's battlefield — only the enchantment
            harness.setHand(player1, List.of());

            advanceToUpkeep(player1);
            harness.passBothPriorities(); // resolve trigger

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("controls no valid creatures"));
        }

        @Test
        @DisplayName("Only offers creatures matching filters as valid choices")
        void filtersRestrictValidChoices() {
            // Stampeding Wildebeests filters to green creatures only
            addPermanent(player1, new StampedingWildebeests());
            Permanent greenCreature = addPermanent(player1, new GrizzlyBears());
            Permanent nonGreenCreature = addPermanent(player1, new SerraAngel());

            advanceToUpkeep(player1);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingPermanentChoiceValidIds())
                    .contains(greenCreature.getId())
                    .doesNotContain(nonGreenCreature.getId());
        }

        @Test
        @DisplayName("Chosen creature is returned to owner's hand and cleared from battlefield")
        void chosenCreatureReturnsToHand() {
            harness.addToBattlefield(player1, new SunkenHope());
            Permanent creature = addPermanent(player1, new GrizzlyBears());

            advanceToUpkeep(player1);
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, creature.getId());

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getId().equals(creature.getId()));
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
            assertThat(gd.interaction.permanentChoiceContext()).isNull();
        }

        @Test
        @DisplayName("Player can choose which creature to bounce when controlling multiple")
        void playerChoosesWhichCreatureToBounce() {
            harness.addToBattlefield(player1, new SunkenHope());
            Permanent creature1 = addPermanent(player1, new GrizzlyBears());
            Permanent creature2 = addPermanent(player1, new SerraAngel());

            advanceToUpkeep(player1);
            harness.passBothPriorities();

            // Choose the second creature
            harness.handlePermanentChosen(player1, creature2.getId());

            // creature2 bounced, creature1 stays
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getId().equals(creature1.getId()));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getId().equals(creature2.getId()));
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Serra Angel"));
        }
    }

    // =========================================================================
    // ReturnSelfToHandOnCoinFlipLossEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveReturnSelfToHandOnCoinFlipLoss")
    class ResolveReturnSelfToHandOnCoinFlipLoss {

        @Test
        @DisplayName("Coin flip is logged and Scoria Wurm ends in exactly one zone")
        void coinFlipLogsAndWurmEndsInOneZone() {
            harness.addToBattlefield(player1, new ScoriaWurm());
            int handBefore = gd.playerHands.get(player1.getId()).size();

            advanceToUpkeep(player1);
            harness.passBothPriorities();

            boolean onBattlefield = gd.playerBattlefields.get(player1.getId()).stream()
                    .anyMatch(p -> p.getCard().getName().equals("Scoria Wurm"));
            boolean inHand = gd.playerHands.get(player1.getId()).stream()
                    .anyMatch(c -> c.getName().equals("Scoria Wurm"));

            // Must be in exactly one zone — battlefield XOR hand
            assertThat(onBattlefield != inHand).isTrue();

            if (inHand) {
                assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
                assertThat(gd.gameLog).anyMatch(log -> log.contains("returned to its owner's hand"));
            } else {
                assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
            }

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("coin flip for Scoria Wurm"));
        }

        @Test
        @DisplayName("Does not trigger during opponent's upkeep")
        void doesNotTriggerDuringOpponentUpkeep() {
            harness.addToBattlefield(player1, new ScoriaWurm());

            advanceToUpkeep(player2);

            assertThat(gd.stack).isEmpty();
        }
    }
}
