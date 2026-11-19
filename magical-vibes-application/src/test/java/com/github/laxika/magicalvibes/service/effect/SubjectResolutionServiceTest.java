package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.c.CurseOfStalkedPrey;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.SubgameResult;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.filter.PlayerControlsMostLandsPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.TestStackDoor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.github.laxika.magicalvibes.model.effect.Subjects.acceptedPlayers;
import static com.github.laxika.magicalvibes.model.effect.Subjects.activePlayer;
import static com.github.laxika.magicalvibes.model.effect.Subjects.allOf;
import static com.github.laxika.magicalvibes.model.effect.Subjects.allTargets;
import static com.github.laxika.magicalvibes.model.effect.Subjects.attached;
import static com.github.laxika.magicalvibes.model.effect.Subjects.attachedAtTrigger;
import static com.github.laxika.magicalvibes.model.effect.Subjects.chosenPlayer;
import static com.github.laxika.magicalvibes.model.effect.Subjects.chosenPlayers;
import static com.github.laxika.magicalvibes.model.effect.Subjects.controlledBy;
import static com.github.laxika.magicalvibes.model.effect.Subjects.controllerOf;
import static com.github.laxika.magicalvibes.model.effect.Subjects.defendingPlayer;
import static com.github.laxika.magicalvibes.model.effect.Subjects.eachOpponent;
import static com.github.laxika.magicalvibes.model.effect.Subjects.eachPlayer;
import static com.github.laxika.magicalvibes.model.effect.Subjects.except;
import static com.github.laxika.magicalvibes.model.effect.Subjects.playersDealtDamageThisResolution;
import static com.github.laxika.magicalvibes.model.effect.Subjects.subgameWinners;
import static com.github.laxika.magicalvibes.model.effect.Subjects.enchantedPlayer;
import static com.github.laxika.magicalvibes.model.effect.Subjects.matching;
import static com.github.laxika.magicalvibes.model.effect.Subjects.named;
import static com.github.laxika.magicalvibes.model.effect.Subjects.opponentsOf;
import static com.github.laxika.magicalvibes.model.effect.Subjects.ownerOf;
import static com.github.laxika.magicalvibes.model.effect.Subjects.returned;
import static com.github.laxika.magicalvibes.model.effect.Subjects.source;
import static com.github.laxika.magicalvibes.model.effect.Subjects.target;
import static com.github.laxika.magicalvibes.model.effect.Subjects.targetPlayer;
import static com.github.laxika.magicalvibes.model.effect.Subjects.targetPlayerDeclaredElsewhere;
import static com.github.laxika.magicalvibes.model.effect.Subjects.targetSpell;
import static com.github.laxika.magicalvibes.model.effect.Subjects.triggering;
import static com.github.laxika.magicalvibes.model.effect.Subjects.triggeringCard;
import static com.github.laxika.magicalvibes.model.effect.Subjects.triggeringPlayer;
import static com.github.laxika.magicalvibes.model.effect.Subjects.triggeringSpell;
import static com.github.laxika.magicalvibes.model.effect.Subjects.you;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Every subject variant resolved against a real game: while the object is on the battlefield, and after it left
 * (last-known information, CR 608.2h; nothing done to what left).
 */
@CardUsed({Forest.class, GrizzlyBears.class, HillGiant.class, Pacifism.class, Bonesplitter.class, CurseOfStalkedPrey.class})
class SubjectResolutionServiceTest extends BaseCardTest {

    private static final CardEffect EFFECT = new DrawCardEffect(1);

    private SubjectResolutionService subjects;

    @BeforeEach
    void setUpService() {
        subjects = GameTestEngineContext.get().getBean(SubjectResolutionService.class);
        harness.forceActivePlayer(player1);
    }

    private StackEntry entry(Player controller, UUID targetId, UUID sourcePermanentId) {
        Card card = sourcePermanentId == null ? new GrizzlyBears()
                : gqs.findPermanentById(gd, sourcePermanentId).getCard();
        StackEntry entry = TestStackDoor.triggered(card, controller.getId(), "Test ability", List.of(EFFECT),
                targetId, sourcePermanentId);
        gd.stack.add(entry);
        return entry;
    }

    private List<Permanent> permanents(StackEntry entry, PermanentSubject subject) {
        return subjects.permanents(gd, entry, EFFECT, subject);
    }

    private List<UUID> players(StackEntry entry, PlayerSubject subject) {
        return subjects.players(gd, entry, EFFECT, subject);
    }

    private void leaveBattlefield(Permanent permanent) {
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent);
    }

    private Permanent attach(Player controller, Card aura, Permanent host) {
        Permanent attachment = addCreatureReady(controller, aura);
        attachment.setAttachedTo(host.getId());
        return attachment;
    }

    @Nested
    @DisplayName("source()")
    class Source {

        @Test
        @DisplayName("Is the entry's source permanent while it is on the battlefield")
        void liveSource() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());

            assertThat(permanents(entry(player1, null, bears.getId()), source())).containsExactly(bears);
        }

        @Test
        @DisplayName("Names nothing to act on once the source left, but keeps its id for last-known reads")
        void sourceThatLeft() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            StackEntry entry = entry(player1, null, bears.getId());
            leaveBattlefield(bears);

            assertThat(permanents(entry, source())).isEmpty();
            assertThat(subjects.permanentIds(gd, entry, EFFECT, source())).containsExactly(bears.getId());
        }

        @Test
        @DisplayName("Is never the entry's target when the entry has no source permanent")
        void noSourceIsNotTheTarget() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());

            assertThat(permanents(entry(player1, bears.getId(), null), source())).isEmpty();
        }
    }

    @Nested
    @DisplayName("target()")
    class Target {

        @Test
        @DisplayName("Is the targeted permanent, and no player target")
        void targetedPermanent() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());

            assertThat(permanents(entry(player1, bears.getId(), null), target())).containsExactly(bears);
            assertThat(permanents(entry(player1, player2.getId(), null), target())).isEmpty();
        }

        @Test
        @DisplayName("Names nothing once the target left the battlefield")
        void targetThatLeft() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            StackEntry entry = entry(player1, bears.getId(), null);
            leaveBattlefield(bears);

            assertThat(permanents(entry, target())).isEmpty();
        }

        @Test
        @DisplayName("Declares a target; a target read inside controllerOf does not")
        void onlyTheTargetVariantDeclaresATarget() {
            assertThat(target().declaresTarget()).isTrue();
            assertThat(targetPlayer().declaresTarget()).isTrue();
            assertThat(controllerOf(target()).declaresTarget()).isFalse();
            assertThat(attached().declaresTarget()).isFalse();
        }
    }

    @Nested
    @DisplayName("allTargets()")
    class AllTargets {

        @Test
        @DisplayName("Is every permanent target of the entry, whichever group, and no player target")
        void everyPermanentTarget() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent giant = addCreatureReady(player1, new HillGiant());
            StackEntry entry = TestStackDoor.triggered(new GrizzlyBears(), player1.getId(), "Test ability",
                    List.of(EFFECT), null, List.of(bears.getId(), player2.getId(), giant.getId()));
            gd.stack.add(entry);

            assertThat(permanents(entry, allTargets())).containsExactly(bears, giant);
            assertThat(allTargets().declaresTarget()).isTrue();
        }

        @Test
        @DisplayName("Is the entry's lone target when it records no target list")
        void loneTarget() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());

            assertThat(permanents(entry(player1, bears.getId(), null), allTargets())).containsExactly(bears);
        }
    }

    @Nested
    @DisplayName("attached()")
    class Attached {

        @Test
        @DisplayName("Is the permanent the source Aura is attached to")
        void enchantedCreature() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent pacifism = attach(player1, new Pacifism(), bears);

            assertThat(permanents(entry(player1, null, pacifism.getId()), attached())).containsExactly(bears);
        }

        @Test
        @DisplayName("Follows the Aura to the creature it enchants now, not the one it enchanted as the ability began")
        void auraMovedAfterActivation() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent giant = addCreatureReady(player2, new HillGiant());
            Permanent pacifism = attach(player1, new Pacifism(), bears);
            StackEntry entry = entry(player1, null, pacifism.getId());
            entry.setSourcePermanentSnapshot(new Permanent(pacifism));

            pacifism.setAttachedTo(giant.getId());

            assertThat(permanents(entry, attached())).containsExactly(giant);
        }

        @Test
        @DisplayName("Is the creature the Aura was attached to as it last existed, once the Aura left")
        void auraThatLeft() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent pacifism = attach(player1, new Pacifism(), bears);
            StackEntry entry = entry(player1, null, pacifism.getId());
            leaveBattlefield(pacifism);

            assertThat(permanents(entry, attached())).containsExactly(bears);
        }

        @Test
        @DisplayName("Is nothing for an Equipment still on the battlefield but unattached")
        void unattachedEquipment() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent bonesplitter = attach(player1, new Bonesplitter(), bears);
            StackEntry entry = entry(player1, null, bonesplitter.getId());
            entry.setAttachedPermanentSnapshot(new Permanent(bears));

            bonesplitter.setAttachedTo(null);

            assertThat(permanents(entry, attached())).isEmpty();
        }

        @Test
        @DisplayName("Names nothing once the enchanted creature left")
        void hostThatLeft() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent pacifism = attach(player1, new Pacifism(), bears);
            StackEntry entry = entry(player1, null, pacifism.getId());
            leaveBattlefield(bears);

            assertThat(permanents(entry, attached())).isEmpty();
        }
    }

    @Nested
    @DisplayName("attachedAtTrigger()")
    class AttachedAtTrigger {

        @Test
        @DisplayName("Is the creature the Aura enchanted when the ability triggered, after the Aura moved, and nothing once it left")
        void hostWhenTriggered() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent giant = addCreatureReady(player2, new HillGiant());
            Permanent pacifism = attach(player1, new Pacifism(), bears);
            StackEntry entry = entry(player1, null, pacifism.getId());
            entry.setAttachedPermanentSnapshot(new Permanent(bears));

            pacifism.setAttachedTo(giant.getId());

            assertThat(permanents(entry, attachedAtTrigger())).containsExactly(bears);

            leaveBattlefield(bears);

            assertThat(permanents(entry, attachedAtTrigger())).isEmpty();
        }

        @Test
        @DisplayName("Is nothing when the entry recorded no host")
        void noRecordedHost() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent pacifism = attach(player1, new Pacifism(), bears);

            assertThat(permanents(entry(player1, null, pacifism.getId()), attachedAtTrigger())).isEmpty();
        }
    }

    @Nested
    @DisplayName("triggering()")
    class Triggering {

        @Test
        @DisplayName("Is the permanent the trigger event was about, and nothing once it left")
        void triggeringPermanent() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            StackEntry entry = entry(player1, null, null);
            entry.setTriggeringPermanentId(bears.getId());

            assertThat(permanents(entry, triggering())).containsExactly(bears);

            leaveBattlefield(bears);

            assertThat(permanents(entry, triggering())).isEmpty();
            assertThat(subjects.permanentIds(gd, entry, EFFECT, triggering())).containsExactly(bears.getId());
        }
    }

    @Nested
    @DisplayName("named(id)")
    class Named {

        @Test
        @DisplayName("Is the bound permanent, whatever the entry's source and target, and nothing once it left")
        void boundPermanent() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent giant = addCreatureReady(player1, new HillGiant());
            StackEntry entry = entry(player1, giant.getId(), giant.getId());

            assertThat(permanents(entry, named(bears.getId()))).containsExactly(bears);

            leaveBattlefield(bears);

            assertThat(permanents(entry, named(bears.getId()))).isEmpty();
        }
    }

    @Nested
    @DisplayName("returned()")
    class Returned {

        @Test
        @DisplayName("Is the permanent the entry's targeted card became")
        void returnedPermanent() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());

            assertThat(permanents(entry(player1, bears.getCard().getId(), null), returned()))
                    .containsExactly(bears);
        }
    }

    @Nested
    @DisplayName("controlledBy(...) and allOf(...)")
    class Sets {

        @Test
        @DisplayName("Finds every matching permanent the players control")
        void controlledByPlayers() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent giant = addCreatureReady(player2, new HillGiant());
            attach(player2, new Pacifism(), giant);
            StackEntry entry = entry(player1, null, null);

            assertThat(permanents(entry, controlledBy(you(), new PermanentIsCreaturePredicate())))
                    .containsExactly(bears);
            assertThat(permanents(entry, controlledBy(eachOpponent(), new PermanentIsCreaturePredicate())))
                    .containsExactly(giant);
            assertThat(permanents(entry, controlledBy(eachPlayer(), new PermanentIsCreaturePredicate())))
                    .containsExactlyInAnyOrder(bears, giant);
        }

        @Test
        @DisplayName("A set's filter knows the triggering player (\"all lands that player controls\")")
        void filterReadsTriggeringPlayer() {
            Permanent ownForest = addCreatureReady(player1, new Forest());
            Permanent theirForest = addCreatureReady(player2, new Forest());
            StackEntry entry = entry(player1, null, null);
            entry.setTriggeringPlayerId(player2.getId());

            assertThat(permanents(entry, controlledBy(eachPlayer(), new PermanentAllOfPredicate(List.of(
                    new PermanentIsLandPredicate(), new PermanentControlledByTriggeringPlayerPredicate())))))
                    .containsExactly(theirForest)
                    .doesNotContain(ownForest);
        }

        @Test
        @DisplayName("A set's filter knows the defending player of the attack")
        void filterReadsDefendingPlayer() {
            addCreatureReady(player1, new GrizzlyBears());
            Permanent giant = addCreatureReady(player2, new HillGiant());
            StackEntry entry = entry(player1, null, null);
            entry.setAttackedTargetId(player2.getId());

            assertThat(permanents(entry, controlledBy(eachPlayer(), new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(), new PermanentControlledByDefendingPlayerPredicate())))))
                    .containsExactly(giant);
        }

        @Test
        @DisplayName("Joins subjects, each permanent once")
        void allOfSubjects() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent giant = addCreatureReady(player2, new HillGiant());
            StackEntry entry = entry(player1, giant.getId(), bears.getId());

            assertThat(permanents(entry, allOf(source(), target()))).containsExactly(bears, giant);
            assertThat(permanents(entry, allOf(source(), controlledBy(you())))).containsExactly(bears);
        }
    }

    @Nested
    @DisplayName("Player subjects")
    class Players {

        @Test
        @DisplayName("you(), targetPlayer() and the active player")
        void youTargetAndActive() {
            StackEntry entry = entry(player1, player2.getId(), null);

            assertThat(players(entry, you())).containsExactly(player1.getId());
            assertThat(players(entry, targetPlayer())).containsExactly(player2.getId());
            assertThat(players(entry, activePlayer())).containsExactly(player1.getId());
        }

        @Test
        @DisplayName("A target player declared elsewhere is read from the entry without declaring a target")
        void targetPlayerDeclaredElsewhereVariant() {
            StackEntry entry = entry(player1, player2.getId(), null);

            assertThat(players(entry, targetPlayerDeclaredElsewhere())).containsExactly(player2.getId());
            assertThat(targetPlayerDeclaredElsewhere().declaresTarget()).isFalse();
            assertThat(targetPlayer().declaresTarget()).isTrue();
        }

        @Test
        @DisplayName("A union of players names a target player itself and a targeted permanent's controller, without declaring a target")
        void allOfPlayersVariant() {
            Permanent giant = addCreatureReady(player2, new HillGiant());
            PlayerSubject playerOrController = allOf(targetPlayerDeclaredElsewhere(), controllerOf(target()));

            assertThat(players(entry(player1, player2.getId(), null), playerOrController))
                    .containsExactly(player2.getId());
            assertThat(players(entry(player1, giant.getId(), null), playerOrController))
                    .containsExactly(player2.getId());
            assertThat(playerOrController.declaresTarget()).isFalse();
            assertThatThrownBy(() -> allOf(targetPlayer(), you()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Opponents of a player are every other player, and nobody when that player is unknown")
        void opponentsOfVariant() {
            StackEntry baked = entry(player1, player2.getId(), null);
            StackEntry none = entry(player1, null, null);

            assertThat(players(baked, opponentsOf(triggeringPlayer()))).containsExactly(player1.getId());
            assertThat(players(none, opponentsOf(triggeringPlayer()))).isEmpty();
        }

        @Test
        @DisplayName("Each player starts with the active player (APNAP order)")
        void eachPlayerInApnapOrder() {
            harness.forceActivePlayer(player2);
            StackEntry entry = entry(player1, null, null);

            assertThat(players(entry, eachPlayer())).containsExactly(player2.getId(), player1.getId());
            assertThat(players(entry, eachOpponent())).containsExactly(player2.getId());
        }

        @Test
        @DisplayName("A filtered set keeps only the players matching the filter as it resolves, ties included")
        void matchingPlayers() {
            harness.addToBattlefield(player2, new Forest());
            StackEntry entry = entry(player1, null, null);
            PlayerSubject mostLands = matching(eachPlayer(), new PlayerControlsMostLandsPredicate());

            assertThat(players(entry, mostLands)).containsExactly(player2.getId());

            harness.addToBattlefield(player1, new Forest());

            assertThat(players(entry, mostLands)).containsExactly(player1.getId(), player2.getId());
        }

        @Test
        @DisplayName("\"That player\" is the recorded triggering player, else the entry's non-targeting player")
        void triggeringPlayerVariant() {
            StackEntry baked = entry(player1, player2.getId(), null);
            StackEntry recorded = entry(player1, null, null);
            recorded.setTriggeringPlayerId(player2.getId());

            assertThat(players(baked, triggeringPlayer())).containsExactly(player2.getId());
            assertThat(players(recorded, triggeringPlayer())).containsExactly(player2.getId());
        }

        @Test
        @DisplayName("Defending player is the player the attack named, or the attacked planeswalker's controller")
        void defendingPlayerVariant() {
            StackEntry entry = entry(player1, null, null);
            entry.setAttackedTargetId(player2.getId());

            assertThat(players(entry, defendingPlayer())).containsExactly(player2.getId());
        }

        @Test
        @DisplayName("Enchanted player is the player the source Curse is attached to, as it is or last was")
        void enchantedPlayerVariant() {
            Permanent curse = addCreatureReady(player1, new CurseOfStalkedPrey());
            curse.setAttachedTo(player2.getId());
            StackEntry entry = entry(player1, null, curse.getId());

            assertThat(players(entry, enchantedPlayer())).containsExactly(player2.getId());
            assertThat(subjects.players(gd, curse, enchantedPlayer())).containsExactly(player2.getId());

            leaveBattlefield(curse);

            assertThat(players(entry, enchantedPlayer())).containsExactly(player2.getId());
        }

        @Test
        @DisplayName("The chosen player is the one the source remembered as it entered")
        void chosenPlayerVariant() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            bears.setRememberedTargetPlayerId(player2.getId());

            assertThat(players(entry(player1, null, bears.getId()), chosenPlayer())).containsExactly(player2.getId());
        }

        @Test
        @DisplayName("A spell's chosen player is the player an earlier effect of the spell chose onto the entry")
        void spellsChosenPlayer() {
            assertThat(players(entry(player1, player2.getId(), null), chosenPlayer()))
                    .containsExactly(player2.getId());
            assertThat(players(entry(player1, null, null), chosenPlayer())).isEmpty();
        }

        @Test
        @DisplayName("Accepted players are those who said yes this resolution; the opponents among them exclude you")
        void acceptedPlayersVariant() {
            StackEntry entry = entry(player1, null, null);
            entry.setAcceptedPlayerIds(List.of(player1.getId(), player2.getId()));

            assertThat(players(entry, acceptedPlayers())).containsExactly(player1.getId(), player2.getId());
            assertThat(players(entry, except(acceptedPlayers(), you()))).containsExactly(player2.getId());
            assertThat(players(entry(player1, null, null), acceptedPlayers())).isEmpty();
        }

        @Test
        @DisplayName("Players dealt damage this resolution are those the entry recorded, in APNAP order")
        void playersDealtDamageVariant() {
            StackEntry entry = entry(player1, null, null);
            entry.recordPlayerDealtDamage(player2.getId());
            entry.recordPlayerDealtDamage(player1.getId());

            assertThat(players(entry, playersDealtDamageThisResolution()))
                    .containsExactly(player1.getId(), player2.getId());
            assertThat(players(entry(player1, null, null), playersDealtDamageThisResolution())).isEmpty();
        }

        @Test
        @DisplayName("Subgame winners are read from the entry's subgame result; without one it is an engine error")
        void subgameWinnersVariant() {
            StackEntry entry = entry(player1, null, null);
            entry.setSubgameResult(new SubgameResult(UUID.randomUUID(), Set.of(player2.getId())));

            assertThat(players(entry, subgameWinners())).containsExactly(player2.getId());
            assertThat(players(entry, except(eachPlayer(), subgameWinners()))).containsExactly(player1.getId());
            assertThatThrownBy(() -> players(entry(player1, null, null), subgameWinners()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("\"That spell's controller\" is read from the stack, else as it was countered; it names no permanent")
        void targetSpellController() {
            Card spellCard = new HillGiant();
            StackEntry spell = new StackEntry(StackEntryType.CREATURE_SPELL, spellCard, player2.getId(),
                    spellCard.getName(), List.of());
            gd.stack.add(spell);
            StackEntry entry = entry(player1, spell.getTargetableId(), null);

            assertThat(players(entry, controllerOf(targetSpell()))).containsExactly(player2.getId());
            assertThat(subjects.permanents(gd, entry, EFFECT, targetSpell())).isEmpty();

            gd.stack.remove(spell);
            assertThat(players(entry, controllerOf(targetSpell()))).isEmpty();
            entry.setCounteredSpellControllerId(player2.getId());
            assertThat(players(entry, controllerOf(targetSpell()))).containsExactly(player2.getId());
        }

        @Test
        @DisplayName("\"That spell's controller\" of a trigger is the controller of the spell the trigger was about")
        void triggeringSpellController() {
            Card spellCard = new HillGiant();
            StackEntry spell = new StackEntry(StackEntryType.CREATURE_SPELL, spellCard, player2.getId(),
                    spellCard.getName(), List.of());
            gd.stack.add(spell);
            StackEntry entry = entry(player1, null, null);

            assertThat(players(entry, controllerOf(triggeringSpell()))).isEmpty();

            entry.setTriggeringCardId(spellCard.getId());
            assertThat(players(entry, controllerOf(triggeringSpell()))).containsExactly(player2.getId());
        }

        @Test
        @DisplayName("The chosen players are every player the source remembered, and the other one excludes that player")
        void chosenPlayersAndExcept() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            bears.getRememberedTargetPlayerIds().addAll(List.of(player2.getId(), player1.getId()));
            StackEntry entry = entry(player1, null, bears.getId());
            entry.setTriggeringPlayerId(player2.getId());

            assertThat(players(entry, chosenPlayers())).containsExactly(player1.getId(), player2.getId());
            assertThat(players(entry, except(chosenPlayers(), triggeringPlayer()))).containsExactly(player1.getId());

            leaveBattlefield(bears);

            assertThat(players(entry, except(chosenPlayers(), triggeringPlayer()))).containsExactly(player1.getId());
        }
    }

    @Nested
    @DisplayName("controllerOf(...) and ownerOf(...)")
    class ControllerAndOwner {

        @Test
        @DisplayName("Its controller is the current controller, then the controller it had as it left")
        void controllerAfterControlChangeAndDeath() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            StackEntry entry = entry(player1, null, null);
            entry.setTriggeringPermanentId(bears.getId());
            entry.setTriggeringPermanentControllerId(player1.getId());
            setOwnerAndController(bears, player1, player2);

            assertThat(players(entry, controllerOf(triggering()))).containsExactly(player2.getId());

            leaveBattlefield(bears);

            assertThat(players(entry, controllerOf(triggering()))).containsExactly(player2.getId());
        }

        @Test
        @DisplayName("A targeted permanent's controller is read without the effect declaring a target")
        void controllerOfTarget() {
            Permanent giant = addCreatureReady(player2, new HillGiant());

            assertThat(players(entry(player1, giant.getId(), null), controllerOf(target())))
                    .containsExactly(player2.getId());
        }

        @Test
        @DisplayName("Its owner is the card's owner, even while another player controls it")
        void ownerOfStolenPermanent() {
            Card card = new GrizzlyBears();
            card.setOwnerId(player1.getId());
            Permanent bears = addCreatureReady(player1, card);
            setOwnerAndController(bears, player1, player2);

            assertThat(players(entry(player2, null, bears.getId()), ownerOf(source())))
                    .containsExactly(player1.getId());
        }

        @Test
        @DisplayName("The triggering card's owner is read from the card, whoever played it")
        void ownerOfTriggeringCard() {
            Card card = new GrizzlyBears();
            card.setOwnerId(player2.getId());
            StackEntry entry = entry(player1, null, null);
            entry.setTriggeringCards(List.of(card));

            assertThat(players(entry, ownerOf(triggeringCard()))).containsExactly(player2.getId());
            assertThat(permanents(entry, triggeringCard())).isEmpty();
        }

        @Test
        @DisplayName("Without a triggering spell or land, the triggering card's owner is the recorded owner, else the triggering permanent's owner")
        void ownerOfTriggeringCardFallsBackToTheTriggeringPermanent() {
            StackEntry recorded = entry(player1, null, null);
            recorded.setTriggeringPermanentOwnerId(player2.getId());

            assertThat(players(recorded, ownerOf(triggeringCard()))).containsExactly(player2.getId());

            Card card = new GrizzlyBears();
            card.setOwnerId(player2.getId());
            Permanent bears = addCreatureReady(player2, card);
            StackEntry entered = entry(player1, null, null);
            entered.setTriggeringPermanentId(bears.getId());

            assertThat(players(entered, ownerOf(triggeringCard()))).containsExactly(player2.getId());
        }

        @Test
        @DisplayName("An owner the card doesn't record is the player it entered under, then its last controller")
        void unrecordedOwner() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            StackEntry entry = entry(player1, null, null);
            entry.setTriggeringPermanentId(bears.getId());

            assertThat(players(entry, ownerOf(triggering()))).containsExactly(player2.getId());

            leaveBattlefield(bears);

            assertThat(players(entry, ownerOf(triggering()))).containsExactly(player2.getId());
        }
    }

    @Nested
    @DisplayName("The static entry point")
    class StaticEntryPoint {

        @Test
        @DisplayName("Resolves the source and what it is attached to")
        void staticSourceAndAttached() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent pacifism = attach(player1, new Pacifism(), bears);

            assertThat(subjects.permanents(gd, pacifism, source())).containsExactly(pacifism);
            assertThat(subjects.permanents(gd, pacifism, attached())).containsExactly(bears);
            assertThat(subjects.players(gd, pacifism, you())).containsExactly(player1.getId());
        }

        @Test
        @DisplayName("Rejects a subject that reads a stack entry")
        void rejectsStackSubjects() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());

            assertThatThrownBy(() -> subjects.permanents(gd, bears, target()))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> subjects.players(gd, bears, controllerOf(triggering())))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
