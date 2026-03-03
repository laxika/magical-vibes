package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.a.ArgentSphinx;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GlimmerpointStag;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntoTheCore;
import com.github.laxika.magicalvibes.cards.m.MimicVat;
import com.github.laxika.magicalvibes.cards.r.RevokeExistence;
import com.github.laxika.magicalvibes.cards.s.SemblanceAnvil;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ExileResolutionServiceTest extends BaseCardTest {

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    // =========================================================================
    // ExileTargetPermanentEffect — single target (via RevokeExistence)
    // =========================================================================

    @Nested
    @DisplayName("resolveExileTargetPermanent — single target")
    class ResolveExileTargetPermanentSingle {

        @Test
        @DisplayName("Exiles target permanent and moves it to owner's exile zone")
        void exilesTargetPermanent() {
            // Use Spellbook (no triggered abilities) to avoid may-ability interrupts
            harness.addToBattlefield(player2, new Spellbook());
            harness.setHand(player1, List.of(new RevokeExistence()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            UUID targetId = harness.getPermanentId(player2, "Spellbook");
            harness.castSorcery(player1, 0, 0, targetId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
            assertThat(gd.playerExiledCards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Spellbook"));
        }

        @Test
        @DisplayName("Does nothing when target permanent is removed before resolution")
        void fizzlesWhenTargetRemoved() {
            harness.addToBattlefield(player2, new Spellbook());
            harness.setHand(player1, List.of(new RevokeExistence()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            UUID targetId = harness.getPermanentId(player2, "Spellbook");
            harness.castSorcery(player1, 0, 0, targetId);

            // Remove target before resolution
            gd.playerBattlefields.get(player2.getId()).clear();

            harness.passBothPriorities();

            assertThat(gd.playerExiledCards.get(player2.getId()))
                    .noneMatch(c -> c.getName().equals("Spellbook"));
        }

        @Test
        @DisplayName("Logs that the permanent is exiled")
        void logsExile() {
            harness.addToBattlefield(player2, new Spellbook());
            harness.setHand(player1, List.of(new RevokeExistence()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            UUID targetId = harness.getPermanentId(player2, "Spellbook");
            harness.castSorcery(player1, 0, 0, targetId);
            harness.passBothPriorities();

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Spellbook") && log.contains("exiled"));
        }
    }

    // =========================================================================
    // ExileTargetPermanentEffect — multi-target (via Into the Core)
    // =========================================================================

    @Nested
    @DisplayName("resolveExileTargetPermanent — multi-target")
    class ResolveExileTargetPermanentMulti {

        @Test
        @DisplayName("Exiles two target artifacts")
        void exilesTwoTargetArtifacts() {
            harness.addToBattlefield(player2, new Spellbook());
            harness.addToBattlefield(player2, new Spellbook());
            harness.setHand(player1, List.of(new IntoTheCore()));
            harness.addMana(player1, ManaColor.RED, 4);

            UUID target1 = gd.playerBattlefields.get(player2.getId()).get(0).getId();
            UUID target2 = gd.playerBattlefields.get(player2.getId()).get(1).getId();
            harness.castInstant(player1, 0, List.of(target1, target2));
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
            assertThat(gd.playerExiledCards.get(player2.getId()))
                    .filteredOn(c -> c.getName().equals("Spellbook"))
                    .hasSize(2);
        }

        @Test
        @DisplayName("Skips targets that were removed before resolution and exiles the rest")
        void skipsRemovedTargets() {
            harness.addToBattlefield(player2, new Spellbook());
            harness.addToBattlefield(player2, new Spellbook());
            harness.setHand(player1, List.of(new IntoTheCore()));
            harness.addMana(player1, ManaColor.RED, 4);

            UUID target1 = gd.playerBattlefields.get(player2.getId()).get(0).getId();
            UUID target2 = gd.playerBattlefields.get(player2.getId()).get(1).getId();
            harness.castInstant(player1, 0, List.of(target1, target2));

            // Remove one target before resolution
            gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(target1));

            harness.passBothPriorities();

            assertThat(gd.playerExiledCards.get(player2.getId()))
                    .filteredOn(c -> c.getName().equals("Spellbook"))
                    .hasSize(1);
        }
    }

    // =========================================================================
    // ExileTargetPermanentAndReturnAtEndStepEffect (via Glimmerpoint Stag)
    // =========================================================================

    @Nested
    @DisplayName("resolveExileTargetPermanentAndReturnAtEndStep")
    class ResolveExileTargetPermanentAndReturnAtEndStep {

        @Test
        @DisplayName("Exiles target permanent and adds a pending exile return")
        void exilesAndSchedulesReturn() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new GlimmerpointStag()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            // GlimmerpointStag requires target at cast time
            gs.playCard(gd, player1, 0, 0, targetId, null);

            // Resolve creature spell → ETB trigger on stack
            harness.passBothPriorities();
            // Resolve ETB trigger
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.pendingExileReturns)
                    .anyMatch(per -> per.card().getName().equals("Grizzly Bears")
                            && per.controllerId().equals(player1.getId()));
        }

        @Test
        @DisplayName("Does nothing when target is removed before resolution")
        void fizzlesWhenTargetRemoved() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new GlimmerpointStag()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
            gs.playCard(gd, player1, 0, 0, bearsId, null);

            // Resolve creature spell → ETB trigger on stack
            harness.passBothPriorities();

            // Remove the bears before the ETB trigger resolves
            gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(bearsId));

            // Resolve ETB → fizzles
            harness.passBothPriorities();

            assertThat(gd.pendingExileReturns)
                    .noneMatch(per -> per.card().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Stolen creature's pending return uses original owner")
        void stolenCreatureUsesOriginalOwner() {
            // Place a creature on player1's battlefield but mark as stolen from player2
            Card bears = new GrizzlyBears();
            Permanent stolen = addPermanent(player1, bears);
            gd.stolenCreatures.put(stolen.getId(), player2.getId());

            harness.setHand(player1, List.of(new GlimmerpointStag()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            gs.playCard(gd, player1, 0, 0, stolen.getId(), null);

            // Resolve creature spell → ETB trigger
            harness.passBothPriorities();
            // Resolve ETB
            harness.passBothPriorities();

            // The pending return should go to the original owner (player2)
            assertThat(gd.pendingExileReturns)
                    .anyMatch(per -> per.card().getName().equals("Grizzly Bears")
                            && per.controllerId().equals(player2.getId()));
        }

        @Test
        @DisplayName("Logs exile and return message")
        void logsExileAndReturn() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new GlimmerpointStag()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            gs.playCard(gd, player1, 0, 0, targetId, null);

            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Grizzly Bears") && log.contains("exiled")
                            && log.contains("return"));
        }
    }

    // =========================================================================
    // ExileSelfAndReturnAtEndStepEffect (via Argent Sphinx)
    // =========================================================================

    @Nested
    @DisplayName("resolveExileSelfAndReturnAtEndStep")
    class ResolveExileSelfAndReturnAtEndStep {

        @Test
        @DisplayName("Exiles self and adds a pending exile return for the controller")
        void exilesSelfAndSchedulesReturn() {
            // Argent Sphinx needs metalcraft (3+ artifacts)
            harness.addToBattlefield(player1, new Spellbook());
            harness.addToBattlefield(player1, new Spellbook());
            harness.addToBattlefield(player1, new Spellbook());
            harness.addToBattlefield(player1, new ArgentSphinx());
            harness.addMana(player1, ManaColor.BLUE, 1);

            int sphinxIndex = gd.playerBattlefields.get(player1.getId()).size() - 1;
            harness.activateAbility(player1, sphinxIndex, null, null);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Argent Sphinx"));
            assertThat(gd.pendingExileReturns)
                    .anyMatch(per -> per.card().getName().equals("Argent Sphinx")
                            && per.controllerId().equals(player1.getId()));
        }

        @Test
        @DisplayName("Does nothing when the source permanent is removed before resolution")
        void fizzlesWhenSourceRemoved() {
            harness.addToBattlefield(player1, new Spellbook());
            harness.addToBattlefield(player1, new Spellbook());
            harness.addToBattlefield(player1, new Spellbook());
            harness.addToBattlefield(player1, new ArgentSphinx());
            harness.addMana(player1, ManaColor.BLUE, 1);

            int sphinxIndex = gd.playerBattlefields.get(player1.getId()).size() - 1;
            harness.activateAbility(player1, sphinxIndex, null, null);

            // Remove the sphinx before the ability resolves
            gd.playerBattlefields.get(player1.getId()).removeIf(
                    p -> p.getCard().getName().equals("Argent Sphinx"));

            harness.passBothPriorities();

            assertThat(gd.pendingExileReturns)
                    .noneMatch(per -> per.card().getName().equals("Argent Sphinx"));
        }

        @Test
        @DisplayName("Logs exile and return message")
        void logsExileAndReturn() {
            harness.addToBattlefield(player1, new Spellbook());
            harness.addToBattlefield(player1, new Spellbook());
            harness.addToBattlefield(player1, new Spellbook());
            harness.addToBattlefield(player1, new ArgentSphinx());
            harness.addMana(player1, ManaColor.BLUE, 1);

            int sphinxIndex = gd.playerBattlefields.get(player1.getId()).size() - 1;
            harness.activateAbility(player1, sphinxIndex, null, null);
            harness.passBothPriorities();

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Argent Sphinx") && log.contains("exiled")
                            && log.contains("return"));
        }
    }

    // =========================================================================
    // ImprintDyingCreatureEffect (via Mimic Vat)
    // =========================================================================

    @Nested
    @DisplayName("resolveImprintDyingCreature")
    class ResolveImprintDyingCreature {

        @Test
        @DisplayName("Imprints a dying creature onto Mimic Vat")
        void imprintsDyingCreature() {
            harness.addToBattlefield(player1, new MimicVat());
            harness.addToBattlefield(player2, new GrizzlyBears());

            // Kill creature with Cruel Edict
            harness.setHand(player1, List.of(new CruelEdict()));
            harness.addMana(player1, ManaColor.BLACK, 2);
            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities(); // Resolve Cruel Edict → creature dies → may trigger

            // Accept the imprint
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities(); // Resolve the imprint effect

            // Grizzly Bears should be exiled (in its owner's exile zone)
            assertThat(gd.playerExiledCards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));

            // Should no longer be in graveyard
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));

            // Mimic Vat should have Grizzly Bears imprinted
            Permanent vat = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Mimic Vat"))
                    .findFirst().orElseThrow();
            assertThat(vat.getCard().getImprintedCard()).isNotNull();
            assertThat(vat.getCard().getImprintedCard().getName()).isEqualTo("Grizzly Bears");
        }

        @Test
        @DisplayName("Replaces previously imprinted card when a new creature dies")
        void replacesPreviousImprint() {
            harness.addToBattlefield(player1, new MimicVat());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player2, new GiantSpider());

            // Kill first creature (Grizzly Bears): player2 has two creatures so must choose
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.setHand(player1, List.of(new CruelEdict()));
            harness.addMana(player1, ManaColor.BLACK, 2);
            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities(); // Resolve Cruel Edict → player2 prompted to choose

            // Player2 chooses to sacrifice Grizzly Bears
            harness.handlePermanentChosen(player2, bearsId);

            // Accept imprint of Grizzly Bears
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();

            Permanent vat = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Mimic Vat"))
                    .findFirst().orElseThrow();
            assertThat(vat.getCard().getImprintedCard().getName()).isEqualTo("Grizzly Bears");

            // Kill second creature (Giant Spider): now player2 has only one, auto-sacrificed
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.setHand(player1, List.of(new CruelEdict()));
            harness.addMana(player1, ManaColor.BLACK, 2);
            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities(); // Resolve Cruel Edict → auto-sacrifice Giant Spider

            // Accept second imprint
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();

            // Grizzly Bears (previously imprinted) should now be in a graveyard
            boolean oldCardInGraveyard = false;
            for (UUID pid : gd.orderedPlayerIds) {
                if (gd.playerGraveyards.get(pid).stream().anyMatch(c -> c.getName().equals("Grizzly Bears"))) {
                    oldCardInGraveyard = true;
                    break;
                }
            }
            assertThat(oldCardInGraveyard).isTrue();

            // Giant Spider should now be imprinted
            assertThat(vat.getCard().getImprintedCard()).isNotNull();
            assertThat(vat.getCard().getImprintedCard().getName()).isEqualTo("Giant Spider");
        }

        @Test
        @DisplayName("Declining may ability does not imprint")
        void decliningMayDoesNotImprint() {
            harness.addToBattlefield(player1, new MimicVat());
            harness.addToBattlefield(player2, new GrizzlyBears());

            harness.setHand(player1, List.of(new CruelEdict()));
            harness.addMana(player1, ManaColor.BLACK, 2);
            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities(); // Resolve Cruel Edict → may trigger

            // Decline the imprint
            harness.handleMayAbilityChosen(player1, false);

            // Grizzly Bears should remain in graveyard
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));

            // Mimic Vat should have nothing imprinted
            Permanent vat = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Mimic Vat"))
                    .findFirst().orElseThrow();
            assertThat(vat.getCard().getImprintedCard()).isNull();
        }

        @Test
        @DisplayName("Logs imprint message")
        void logsImprint() {
            harness.addToBattlefield(player1, new MimicVat());
            harness.addToBattlefield(player2, new GrizzlyBears());

            harness.setHand(player1, List.of(new CruelEdict()));
            harness.addMana(player1, ManaColor.BLACK, 2);
            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Grizzly Bears") && log.contains("imprinted"));
        }
    }

    // =========================================================================
    // ExileFromHandToImprintEffect (via Semblance Anvil)
    // =========================================================================

    @Nested
    @DisplayName("resolveExileFromHandToImprint")
    class ResolveExileFromHandToImprint {

        @Test
        @DisplayName("Prompts player to choose a card from hand to imprint")
        void promptsCardChoice() {
            harness.setHand(player1, List.of(new SemblanceAnvil(), new GrizzlyBears()));
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            harness.castArtifact(player1, 0);
            harness.passBothPriorities(); // Resolve Anvil → ETB may trigger

            // Accept the may ability
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities(); // Resolve the imprint effect

            // Should now be awaiting card choice from hand
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.IMPRINT_FROM_HAND_CHOICE);
        }

        @Test
        @DisplayName("Skips when controller has no matching cards in hand")
        void skipsWhenNoMatchingCards() {
            // Semblance Anvil with empty hand after casting (no nonland cards left)
            harness.setHand(player1, List.of(new SemblanceAnvil()));
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            harness.castArtifact(player1, 0);
            harness.passBothPriorities(); // Resolve Anvil → ETB may trigger

            // Accept may — but there are no nonland cards in hand
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities(); // Resolve the imprint effect → no valid cards → skip

            // Should not be waiting for imprint choice
            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.IMPRINT_FROM_HAND_CHOICE);
        }

        @Test
        @DisplayName("Declining may ability skips imprint entirely")
        void decliningMaySkipsImprint() {
            harness.setHand(player1, List.of(new SemblanceAnvil(), new GrizzlyBears()));
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            harness.castArtifact(player1, 0);
            harness.passBothPriorities(); // Resolve Anvil → ETB may trigger

            // Decline the may ability
            harness.handleMayAbilityChosen(player1, false);

            // Grizzly Bears should still be in hand
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));

            // Anvil should have nothing imprinted
            Permanent anvil = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Semblance Anvil"))
                    .findFirst().orElseThrow();
            assertThat(anvil.getCard().getImprintedCard()).isNull();
        }
    }
}
