package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.model.effect.CardSubject;
import com.github.laxika.magicalvibes.cards.c.CurseOfStalkedPrey;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenCardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasTypeChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithPermanentsPredicate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.SubgameResult;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject.OtherPermanentsSharing.SharedQuality;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTargetCardPredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectSupport;
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
import static com.github.laxika.magicalvibes.model.effect.Subjects.targetAbilitySource;
import static com.github.laxika.magicalvibes.model.effect.Subjects.allTargets;
import static com.github.laxika.magicalvibes.model.effect.Subjects.attached;
import static com.github.laxika.magicalvibes.model.effect.Subjects.attachedOrSource;
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
import static com.github.laxika.magicalvibes.model.effect.Subjects.exiledThisResolution;
import static com.github.laxika.magicalvibes.model.effect.Subjects.departedThisWay;
import static com.github.laxika.magicalvibes.model.effect.Subjects.targetDeclaredElsewhere;
import static com.github.laxika.magicalvibes.model.effect.Subjects.oneOf;
import static com.github.laxika.magicalvibes.model.effect.Subjects.returned;
import static com.github.laxika.magicalvibes.model.effect.Subjects.returnedThisResolution;
import static com.github.laxika.magicalvibes.model.effect.Subjects.source;
import static com.github.laxika.magicalvibes.model.effect.Subjects.otherCreaturesSharing;
import static com.github.laxika.magicalvibes.model.effect.Subjects.otherPermanentsSharing;
import static com.github.laxika.magicalvibes.model.effect.Subjects.target;
import static com.github.laxika.magicalvibes.model.effect.Subjects.targetPlayer;
import static com.github.laxika.magicalvibes.model.effect.Subjects.targetPlayerDeclaredElsewhere;
import static com.github.laxika.magicalvibes.model.effect.Subjects.targetSpell;
import static com.github.laxika.magicalvibes.model.effect.Subjects.triggering;
import static com.github.laxika.magicalvibes.model.effect.Subjects.triggeringAll;
import static com.github.laxika.magicalvibes.model.effect.Subjects.triggeringOrSource;
import static com.github.laxika.magicalvibes.model.effect.Subjects.triggeringCard;
import static com.github.laxika.magicalvibes.model.effect.Subjects.triggeringPlayer;
import static com.github.laxika.magicalvibes.model.effect.Subjects.triggeringSpell;
import static com.github.laxika.magicalvibes.model.effect.Subjects.attachedCreature;
import static com.github.laxika.magicalvibes.model.effect.Subjects.lastAttachedCreature;
import static com.github.laxika.magicalvibes.model.effect.Subjects.bandmatesOf;
import static com.github.laxika.magicalvibes.model.effect.Subjects.createdThisResolution;
import static com.github.laxika.magicalvibes.model.effect.Subjects.triggeringAttackers;
import static com.github.laxika.magicalvibes.model.effect.Subjects.triggeringSpellTargets;
import static com.github.laxika.magicalvibes.model.effect.Subjects.withCreaturesSharingAColor;
import static com.github.laxika.magicalvibes.model.effect.Subjects.you;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Every subject variant resolved against a real game: while the object is on the battlefield, and after it left
 * (last-known information, CR 608.2h; nothing done to what left).
 */
@CardUsed({Forest.class, GrizzlyBears.class, HillGiant.class, Pacifism.class, RuneclawBear.class, Bonesplitter.class, CurseOfStalkedPrey.class})
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
    @DisplayName("attachedToPlayers(players, filter)")
    class AttachedToPlayers {

        @Test
        @DisplayName("Names each matching permanent attached to one of the players, whoever controls it")
        void attachedToTheNamedPlayers() {
            Permanent onOpponent = harness.addToBattlefieldAndReturn(player1, new CurseOfStalkedPrey());
            onOpponent.setAttachedTo(player2.getId());
            Permanent opponentsOwn = harness.addToBattlefieldAndReturn(player2, new CurseOfStalkedPrey());
            opponentsOwn.setAttachedTo(player2.getId());
            Permanent onYou = harness.addToBattlefieldAndReturn(player2, new CurseOfStalkedPrey());
            onYou.setAttachedTo(player1.getId());
            Permanent creatureAura = attach(player1, new Pacifism(), addCreatureReady(player2, new GrizzlyBears()));

            assertThat(permanents(entry(player1, null, null), Subjects.attachedToPlayers(eachOpponent(),
                    new PermanentHasSubtypePredicate(CardSubtype.CURSE))))
                    .containsExactlyInAnyOrder(onOpponent, opponentsOwn)
                    .doesNotContain(onYou, creatureAura);
        }

        @Test
        @DisplayName("Binds a same-name card filter to the names of what it names as the effect applies")
        void bindsSameNameFilter() {
            Permanent onOpponent = harness.addToBattlefieldAndReturn(player1, new CurseOfStalkedPrey());
            onOpponent.setAttachedTo(player2.getId());
            StackEntry entry = entry(player1, null, null);
            CardPredicate sameName = new CardSharesNameWithPermanentsPredicate(Subjects.attachedToPlayers(
                    eachOpponent(), new PermanentHasSubtypePredicate(CardSubtype.CURSE)));
            PredicateEvaluationService predicates =
                    GameTestEngineContext.get().getBean(PredicateEvaluationService.class);

            CardPredicate bound = subjects.bindCardSubjects(gd, entry, EFFECT, sameName);

            assertThat(predicates.matchesCardPredicate(new CurseOfStalkedPrey(), bound, null, gd, player1.getId()))
                    .isTrue();
            assertThat(predicates.matchesCardPredicate(new Pacifism(), bound, null, gd, player1.getId())).isFalse();
            onOpponent.setAttachedTo(player1.getId());
            assertThat(predicates.matchesCardPredicate(new CurseOfStalkedPrey(),
                    subjects.bindCardSubjects(gd, entry, EFFECT, sameName), null, gd, player1.getId())).isFalse();
        }
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
    @DisplayName("namesPlayer(gameData, source, controller, player, controllerOf(attached()))")
    class StaticEnchantedPermanentController {

        @Test
        @DisplayName("Names the enchanted permanent's controller, not the Aura's")
        void enchantedPermanentController() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent pacifism = attach(player1, new Pacifism(), bears);
            PlayerSubject subject = Subjects.controllerOf(attached());

            assertThat(SubjectResolutionService.namesPlayer(gd, pacifism, player1.getId(), player2.getId(), subject))
                    .isTrue();
            assertThat(SubjectResolutionService.namesPlayer(gd, pacifism, player1.getId(), player1.getId(), subject))
                    .isFalse();
        }

        @Test
        @DisplayName("Names nobody while the source is attached to nothing")
        void unattached() {
            Permanent pacifism = addCreatureReady(player1, new Pacifism());

            assertThat(SubjectResolutionService.namesPlayer(gd, pacifism, player1.getId(), player1.getId(),
                    Subjects.controllerOf(attached()))).isFalse();
        }
    }

    @Nested
    @DisplayName("namesPlayer(gameData, source, controller, player, ownerOf(source()) / controllerOf(source()))")
    class StaticSourceOwnerAndController {

        @Test
        @DisplayName("ownerOf(source()) names the source card's current owner, not its controller")
        void sourceOwner() {
            GrizzlyBears card = new GrizzlyBears();
            card.setOwnerId(player1.getId());
            Permanent bears = addCreatureReady(player2, card);
            PlayerSubject subject = Subjects.ownerOf(source());

            assertThat(SubjectResolutionService.namesPlayer(gd, bears, player2.getId(), player1.getId(), subject))
                    .isTrue();
            assertThat(SubjectResolutionService.namesPlayer(gd, bears, player2.getId(), player2.getId(), subject))
                    .isFalse();

            gd.cardOwnerOverrides.put(card.getId(), player2.getId());

            assertThat(SubjectResolutionService.namesPlayer(gd, bears, player2.getId(), player2.getId(), subject))
                    .isTrue();
        }

        @Test
        @DisplayName("controllerOf(source()) names the ability's controller")
        void sourceController() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            PlayerSubject subject = Subjects.controllerOf(source());

            assertThat(SubjectResolutionService.namesPlayer(bears, player2.getId(), player2.getId(), subject)).isTrue();
            assertThat(SubjectResolutionService.namesPlayer(bears, player2.getId(), player1.getId(), subject)).isFalse();
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

        @Test
        @DisplayName("Names nothing once the Aura went to the graveyard unattached after its creature left first")
        void auraThatLeftUnattachedAfterItsHost() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent pacifism = attach(player1, new Pacifism(), bears);
            StackEntry entry = entry(player1, null, pacifism.getId());
            entry.setAttachedPermanentSnapshot(new Permanent(bears));
            leaveBattlefield(bears);
            pacifism.setAttachedTo(null);
            leaveBattlefield(pacifism);

            assertThat(subjects.permanentIds(gd, entry, EFFECT, attached())).isEmpty();
            assertThat(subjects.permanentIds(gd, entry, EFFECT, attachedCreature())).isEmpty();
        }
    }

    @Nested
    @DisplayName("lastAttachedCreature()")
    class LastAttachedCreature {

        @Test
        @DisplayName("Is the creature the Aura is attached to while the Aura is on the battlefield")
        void enchantedCreature() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent giant = addCreatureReady(player2, new HillGiant());
            Permanent pacifism = attach(player1, new Pacifism(), bears);
            StackEntry entry = entry(player1, null, pacifism.getId());
            entry.setAttachedPermanentSnapshot(new Permanent(bears));

            pacifism.setAttachedTo(giant.getId());

            assertThat(permanents(entry, lastAttachedCreature())).containsExactly(giant);
        }

        @Test
        @DisplayName("Is the creature the Aura was last enchanting once the Aura went to the graveyard unattached after that creature left")
        void auraThatLeftUnattachedAfterItsHost() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent pacifism = attach(player1, new Pacifism(), bears);
            StackEntry entry = entry(player1, null, pacifism.getId());
            entry.setAttachedPermanentSnapshot(new Permanent(bears));
            leaveBattlefield(bears);
            pacifism.setAttachedTo(null);
            leaveBattlefield(pacifism);

            assertThat(subjects.permanentIds(gd, entry, EFFECT, lastAttachedCreature())).containsExactly(bears.getId());
        }

        @Test
        @DisplayName("Is the creature the Aura was last enchanting when the Aura fell off it and that creature stayed")
        void auraThatFellOffAHostThatStayed() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent pacifism = attach(player1, new Pacifism(), bears);
            StackEntry entry = entry(player1, null, pacifism.getId());
            entry.setAttachedPermanentSnapshot(new Permanent(bears));
            pacifism.setAttachedTo(null);
            leaveBattlefield(pacifism);

            assertThat(permanents(entry, lastAttachedCreature())).containsExactly(bears);
            assertThat(permanents(entry, attachedCreature())).isEmpty();
        }

        @Test
        @DisplayName("Is the creature the Aura was attached to as it last existed, once the Aura left still attached")
        void auraThatLeftAttached() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent giant = addCreatureReady(player2, new HillGiant());
            Permanent pacifism = attach(player1, new Pacifism(), bears);
            StackEntry entry = entry(player1, null, pacifism.getId());
            entry.setAttachedPermanentSnapshot(new Permanent(giant));
            leaveBattlefield(pacifism);

            assertThat(permanents(entry, lastAttachedCreature())).containsExactly(bears);
        }
    }

    @Nested
    @DisplayName("attachedOrSource()")
    class AttachedOrSource {

        @Test
        @DisplayName("Is the host while the source is attached, and the source itself once it is unattached")
        void hostElseSource() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent bonesplitter = attach(player1, new Bonesplitter(), bears);
            StackEntry entry = entry(player1, null, bonesplitter.getId());

            assertThat(permanents(entry, attachedOrSource())).containsExactly(bears);

            bonesplitter.setAttachedTo(null);

            assertThat(permanents(entry, attachedOrSource())).containsExactly(bonesplitter);
        }

        @Test
        @DisplayName("Is the creature the source was attached to as it last existed, once the source left")
        void sourceThatLeftAttached() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent pacifism = attach(player1, new Pacifism(), bears);
            StackEntry entry = entry(player1, null, pacifism.getId());
            leaveBattlefield(pacifism);

            assertThat(permanents(entry, attachedOrSource())).containsExactly(bears);
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

        @Test
        @DisplayName("triggeringOrSource() is the recorded permanent, else the source of its own event")
        void triggeringOrSourcePermanent() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent giant = addCreatureReady(player1, new HillGiant());
            StackEntry own = entry(player1, null, giant.getId());
            StackEntry about = entry(player1, null, giant.getId());
            about.setTriggeringPermanentId(bears.getId());

            assertThat(permanents(own, triggeringOrSource())).containsExactly(giant);
            assertThat(permanents(about, triggeringOrSource())).containsExactly(bears);
        }

        @Test
        @DisplayName("triggeringAll() is every permanent of a \"one or more\" event, else the one the event was about")
        void triggeringAllPermanents() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent giant = addCreatureReady(player1, new HillGiant());
            StackEntry single = entry(player1, null, null);
            single.setTriggeringPermanentId(bears.getId());
            StackEntry group = entry(player1, null, null);
            group.setTriggeringPermanentId(bears.getId());
            group.setTriggeringPermanentIds(List.of(bears.getId(), giant.getId()));

            assertThat(permanents(single, triggeringAll())).containsExactly(bears);
            assertThat(permanents(group, triggeringAll())).containsExactly(bears, giant);

            leaveBattlefield(giant);

            assertThat(permanents(group, triggeringAll())).containsExactly(bears);
            assertThat(subjects.permanentIds(gd, group, EFFECT, triggeringAll()))
                    .containsExactly(bears.getId(), giant.getId());
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
    @DisplayName("returnedThisResolution()")
    class ReturnedThisResolution {

        @Test
        @DisplayName("Is every permanent the entry recorded as returned that is still on the battlefield")
        void recordedReturns() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent giant = addCreatureReady(player2, new HillGiant());
            StackEntry entry = entry(player1, null, null);
            entry.rememberReturnedPermanent(bears.getId());
            entry.rememberReturnedPermanent(giant.getId());

            assertThat(permanents(entry, returnedThisResolution())).containsExactly(bears, giant);

            leaveBattlefield(giant);

            assertThat(permanents(entry, returnedThisResolution())).containsExactly(bears);
        }
    }

    @Nested
    @DisplayName("exiledThisResolution()")
    class ExiledThisResolution {

        @Test
        @DisplayName("Names no permanent on the battlefield, only the id of the entry's target as it left")
        void namesTheExiledTargetsId() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            StackEntry entry = entry(player1, bears.getId(), null);
            harness.getPermanentRemovalService().removePermanentToExile(gd, bears);

            assertThat(permanents(entry, exiledThisResolution())).isEmpty();
            assertThat(subjects.permanentIds(gd, entry, EFFECT, exiledThisResolution()))
                    .containsExactly(bears.getId());
        }
    }

    @Nested
    @DisplayName("departedThisWay(kind)")
    class DepartedThisWay {

        @Test
        @DisplayName("Names the recorded permanents that left the battlefield, none still there, and duplicates each as it last was")
        void recordedDepartures() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent giant = addCreatureReady(player2, new HillGiant());
            StackEntry entry = entry(player1, null, null);
            entry.recordStep(StepKind.DESTROY, StepOutcome.ofObjects(List.of(bears.getId(), giant.getId()),
                    List.of(player2.getId())));
            leaveBattlefield(bears);

            assertThat(permanents(entry, departedThisWay(StepKind.DESTROY))).isEmpty();
            assertThat(subjects.permanentIds(gd, entry, EFFECT, departedThisWay(StepKind.DESTROY)))
                    .containsExactly(bears.getId());
            assertThat(subjects.cardsToDuplicate(gd, entry, EFFECT, departedThisWay(StepKind.DESTROY)))
                    .extracting(Card::getName)
                    .containsExactly("Grizzly Bears");
        }

        @Test
        @DisplayName("Is only for a step that moves permanents off the battlefield, and declares no target")
        void onlyRemovalSteps() {
            assertThat(departedThisWay(StepKind.RETURN).declaresTarget()).isFalse();
            assertThatThrownBy(() -> departedThisWay(StepKind.DEAL_DAMAGE))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("picked(), picked(filter)")
    class Picked {

        @Test
        @DisplayName("Names the entry's picks still on the battlefield, narrowed by the filter, and declares no target")
        void namesThePicks() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent giant = addCreatureReady(player1, new HillGiant());
            Permanent other = addCreatureReady(player1, new HillGiant());
            StackEntry entry = entry(player1, null, null);
            entry.recordPicks(List.of(bears.getId(), giant.getId()));

            assertThat(permanents(entry, Subjects.picked())).containsExactly(bears, giant);
            assertThat(permanents(entry, Subjects.picked(new PermanentHasSubtypePredicate(CardSubtype.GIANT))))
                    .containsExactly(giant)
                    .doesNotContain(other);
            leaveBattlefield(giant);
            assertThat(permanents(entry, Subjects.picked())).containsExactly(bears);
            assertThat(Subjects.picked().declaresTarget()).isFalse();
        }
    }

    @Nested
    @DisplayName("targetDeclaredElsewhere()")
    class TargetDeclaredElsewhere {

        @Test
        @DisplayName("Is the entry's target without declaring one, and duplicates it as it last was once it left")
        void readsTheTargetWithoutDeclaring() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            StackEntry entry = entry(player1, bears.getId(), null);

            assertThat(targetDeclaredElsewhere().declaresTarget()).isFalse();
            assertThat(permanents(entry, targetDeclaredElsewhere())).containsExactly(bears);

            entry.rememberLastKnownPermanentCard(bears.getId(), bears.getCard());
            leaveBattlefield(bears);

            assertThat(permanents(entry, targetDeclaredElsewhere())).isEmpty();
            assertThat(subjects.cardsToDuplicate(gd, entry, EFFECT, targetDeclaredElsewhere()))
                    .extracting(Card::getName)
                    .containsExactly("Grizzly Bears");
        }
    }

    @Nested
    @DisplayName("attachedCreature()")
    class AttachedCreature {

        @Test
        @DisplayName("Is the creature the source Aura is attached to")
        void enchantedCreature() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent pacifism = attach(player1, new Pacifism(), bears);

            assertThat(permanents(entry(player1, null, pacifism.getId()), attachedCreature())).containsExactly(bears);
        }

        @Test
        @DisplayName("Names a host that is not a creature only as \"enchanted permanent\", not as \"enchanted creature\"")
        void noncreatureHost() {
            Permanent forest = addCreatureReady(player2, new Forest());
            Permanent pacifism = attach(player1, new Pacifism(), forest);
            StackEntry entry = entry(player1, null, pacifism.getId());

            assertThat(permanents(entry, attachedCreature())).isEmpty();
            assertThat(permanents(entry, attached())).containsExactly(forest);
        }
    }

    @Nested
    @DisplayName("triggeringAttackers()")
    class TriggeringAttackers {

        @Test
        @DisplayName("Is every attacker the entry recorded as the ability triggered, those still on the battlefield")
        void recordedAttackers() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent giant = addCreatureReady(player1, new HillGiant());
            addCreatureReady(player1, new GrizzlyBears());
            StackEntry entry = entry(player1, null, null);
            entry.setAttackingPermanentSnapshots(List.of(new Permanent(bears), new Permanent(giant)));

            assertThat(permanents(entry, triggeringAttackers())).containsExactly(bears, giant);
            assertThat(triggeringAttackers().declaresTarget()).isFalse();

            leaveBattlefield(giant);

            assertThat(permanents(entry, triggeringAttackers())).containsExactly(bears);
        }
    }

    @Nested
    @DisplayName("triggeringSpellTargets()")
    class TriggeringSpellTargets {

        @Test
        @DisplayName("Is the targets of the triggering spell as it is on the stack")
        void spellOnTheStack() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent giant = addCreatureReady(player1, new HillGiant());
            StackEntry spell = entry(player1, giant.getId(), null);
            StackEntry entry = entry(player1, null, null);
            entry.setTriggeringCardId(spell.getTargetableId());
            entry.setTriggeringSpellTargetIds(List.of(bears.getId()));

            assertThat(permanents(entry, triggeringSpellTargets())).containsExactly(giant);
        }

        @Test
        @DisplayName("Is the targets the entry recorded once the spell left the stack, and declares no target")
        void recordedTargets() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            StackEntry entry = entry(player1, null, null);
            entry.setTriggeringSpellTargetIds(List.of(bears.getId()));

            assertThat(permanents(entry, triggeringSpellTargets())).containsExactly(bears);
            assertThat(triggeringSpellTargets().declaresTarget()).isFalse();
        }
    }

    @Nested
    @DisplayName("createdThisResolution()")
    class CreatedThisResolution {

        @Test
        @DisplayName("Is every permanent the entry recorded as created that is still on the battlefield")
        void recordedTokens() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent giant = addCreatureReady(player1, new HillGiant());
            addCreatureReady(player1, new GrizzlyBears());
            StackEntry entry = entry(player1, null, null);
            entry.getCreatedPermanentIds().add(bears.getId());
            entry.getCreatedPermanentIds().add(giant.getId());

            assertThat(permanents(entry, createdThisResolution())).containsExactly(bears, giant);

            leaveBattlefield(bears);

            assertThat(permanents(entry, createdThisResolution())).containsExactly(giant);
        }
    }

    @Nested
    @DisplayName("bandmatesOf(source())")
    class BandmatesOf {

        @Test
        @DisplayName("Is the other attacking creatures in the source's band, not the source or other attackers")
        void otherBandMembers() {
            Permanent source = addCreatureReady(player1, new GrizzlyBears());
            Permanent bandmate = addCreatureReady(player1, new HillGiant());
            Permanent loneAttacker = addCreatureReady(player1, new GrizzlyBears());
            UUID band = UUID.randomUUID();
            for (Permanent attacker : List.of(source, bandmate, loneAttacker)) {
                attacker.setAttacking(true);
            }
            source.setBandId(band);
            bandmate.setBandId(band);
            StackEntry entry = entry(player1, null, source.getId());

            assertThat(permanents(entry, bandmatesOf(source()))).containsExactly(bandmate);
        }

        @Test
        @DisplayName("Is nothing when the source is not attacking in a band")
        void notInABand() {
            Permanent source = addCreatureReady(player1, new GrizzlyBears());
            Permanent attacker = addCreatureReady(player1, new HillGiant());
            attacker.setAttacking(true);
            attacker.setBandId(UUID.randomUUID());

            assertThat(permanents(entry(player1, null, source.getId()), bandmatesOf(source()))).isEmpty();
        }
    }

    @Nested
    @DisplayName("withCreaturesSharingAColor(target())")
    class WithCreaturesSharingAColor {

        @Test
        @DisplayName("Is the target creature and every creature sharing a color with it, and declares the target")
        void targetAndSharingCreatures() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent otherBears = addCreatureReady(player1, new GrizzlyBears());
            addCreatureReady(player1, new HillGiant());

            assertThat(permanents(entry(player1, bears.getId(), null), withCreaturesSharingAColor(target())))
                    .containsExactlyInAnyOrder(bears, otherBears);
            assertThat(withCreaturesSharingAColor(target()).declaresTarget()).isTrue();
        }

        @Test
        @DisplayName("Is nothing once the target left the battlefield")
        void targetLeft() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            addCreatureReady(player1, new GrizzlyBears());
            StackEntry entry = entry(player1, bears.getId(), null);
            leaveBattlefield(bears);

            assertThat(permanents(entry, withCreaturesSharingAColor(target()))).isEmpty();
        }
    }

    @Nested
    @DisplayName("otherCreaturesSharing(target(), quality) and otherPermanentsSharing(target(), quality, filter)")
    class OtherPermanentsSharing {

        @Test
        @DisplayName("A color: every other creature sharing a color with the target, not the target, and declares no target")
        void color() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent otherBears = addCreatureReady(player1, new GrizzlyBears());
            Permanent runeclaw = addCreatureReady(player1, new RuneclawBear());
            addCreatureReady(player1, new HillGiant());
            PermanentSubject subject = otherCreaturesSharing(target(), SharedQuality.COLOR);

            assertThat(permanents(entry(player1, bears.getId(), null), subject))
                    .containsExactlyInAnyOrder(otherBears, runeclaw);
            assertThat(subject.declaresTarget()).isFalse();
        }

        @Test
        @DisplayName("A name: only the other creatures with the target's name")
        void name() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent otherBears = addCreatureReady(player1, new GrizzlyBears());
            addCreatureReady(player1, new RuneclawBear());

            assertThat(permanents(entry(player1, bears.getId(), null),
                    otherCreaturesSharing(target(), SharedQuality.NAME))).containsExactly(otherBears);
        }

        @Test
        @DisplayName("A creature type: the other creatures sharing one of the target's creature types")
        void creatureType() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent runeclaw = addCreatureReady(player1, new RuneclawBear());
            addCreatureReady(player1, new HillGiant());

            assertThat(permanents(entry(player1, bears.getId(), null),
                    otherCreaturesSharing(target(), SharedQuality.CREATURE_TYPE))).containsExactly(runeclaw);
        }

        @Test
        @DisplayName("Is nothing once the target left the battlefield")
        void targetLeft() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            addCreatureReady(player1, new GrizzlyBears());
            StackEntry entry = entry(player1, bears.getId(), null);
            leaveBattlefield(bears);

            assertThat(permanents(entry, otherCreaturesSharing(target(), SharedQuality.COLOR))).isEmpty();
        }

        @Test
        @DisplayName("A mana value: the other permanents matching the filter with the target's mana value")
        void manaValue() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent runeclaw = addCreatureReady(player1, new RuneclawBear());
            addCreatureReady(player1, new HillGiant());
            addCreatureReady(player1, new Forest());

            assertThat(permanents(entry(player1, bears.getId(), null), otherPermanentsSharing(target(),
                    SharedQuality.MANA_VALUE, new PermanentNotPredicate(new PermanentIsLandPredicate()))))
                    .containsExactly(runeclaw);
        }

        @Test
        @DisplayName("Any permanent: the other lands with a land target's name")
        void nameOfALand() {
            Permanent forest = addCreatureReady(player2, new Forest());
            Permanent otherForest = addCreatureReady(player1, new Forest());
            addCreatureReady(player1, new GrizzlyBears());

            assertThat(permanents(entry(player1, forest.getId(), null),
                    otherPermanentsSharing(target(), SharedQuality.NAME, new PermanentIsLandPredicate())))
                    .containsExactly(otherForest);
        }

        @Test
        @DisplayName("Names only the other permanents the filter matches")
        void filterNarrowsTheOthers() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            bears.tap();
            addCreatureReady(player1, new GrizzlyBears());
            Permanent tappedBears = addCreatureReady(player1, new GrizzlyBears());
            tappedBears.tap();

            assertThat(permanents(entry(player1, bears.getId(), null),
                    otherPermanentsSharing(target(), SharedQuality.NAME, new PermanentIsTappedPredicate())))
                    .containsExactly(tappedBears);
        }

        @Test
        @DisplayName("Is nothing when the named permanent no longer matches the filter")
        void namedPermanentOutsideTheFilter() {
            Permanent forest = addCreatureReady(player2, new Forest());
            addCreatureReady(player1, new Forest());

            assertThat(permanents(entry(player1, forest.getId(), null),
                    otherCreaturesSharing(target(), SharedQuality.NAME))).isEmpty();
        }
    }

    @Nested
    @DisplayName("oneOf(...)")
    class OneOf {

        @Test
        @DisplayName("Is a choice the verb's handler makes, so the resolver refuses it")
        void refusedByTheResolver() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            StackEntry entry = entry(player1, null, bears.getId());

            assertThatThrownBy(() -> permanents(entry, oneOf(source(), target())))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("controlledBy(...) and allOf(...)")
    class Sets {

        @Test
        @DisplayName("\"Each other creature\" leaves out the entry's target permanent as the effect resolves")
        void otherThanTheTarget() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent giant = addCreatureReady(player2, new HillGiant());

            assertThat(permanents(entry(player1, bears.getId(), null), controlledBy(eachPlayer(),
                    new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                            new PermanentNotPredicate(new PermanentIsTargetCardPredicate()))))))
                    .containsExactly(giant);
        }

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
    @DisplayName("lastingRule: a set as a rule that reaches later arrivals")
    class LastingRule {

        private boolean matches(PermanentPredicate rule, Permanent permanent) {
            return GameTestEngineContext.get().getBean(PredicateEvaluationService.class)
                    .matchesPermanentPredicate(gd, permanent, rule);
        }

        private PermanentPredicate rule(StackEntry entry, PermanentSubject set) {
            return subjects.lastingRule(gd, entry, EFFECT, (PermanentSubject.ControlledBy) set);
        }

        @Test
        @DisplayName("Binds the target player now and judges the filter later, on creatures entering afterwards")
        void bindsPlayersAndCoversLaterArrivals() {
            StackEntry entry = entry(player1, player2.getId(), null);

            PermanentPredicate rule = rule(entry, controlledBy(targetPlayer(), new PermanentIsCreaturePredicate()));
            Permanent laterGiant = addCreatureReady(player2, new HillGiant());
            Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
            Permanent theirForest = addCreatureReady(player2, new Forest());

            assertThat(matches(rule, laterGiant)).isTrue();
            assertThat(matches(rule, ownBears)).isFalse();
            assertThat(matches(rule, theirForest)).isFalse();
        }

        @Test
        @DisplayName("Every player's set binds no player: the rule is the set's filter")
        void eachPlayerBindsNoPlayer() {
            StackEntry entry = entry(player1, null, null);
            PermanentPredicate creature = new PermanentIsCreaturePredicate();

            assertThat(rule(entry, controlledBy(eachPlayer(), creature))).isSameAs(creature);
        }

        @Test
        @DisplayName("Binds \"other than the target\" to the target permanent (\"other creatures that player controls\")")
        void bindsTheTarget() {
            Permanent marked = addCreatureReady(player2, new GrizzlyBears());
            StackEntry entry = entry(player1, marked.getId(), null);

            PermanentPredicate rule = rule(entry, controlledBy(controllerOf(target()), new PermanentAllOfPredicate(
                    List.of(new PermanentIsCreaturePredicate(),
                            new PermanentNotPredicate(new PermanentIsTargetCardPredicate())))));
            Permanent laterGiant = addCreatureReady(player2, new HillGiant());

            assertThat(matches(rule, marked)).isFalse();
            assertThat(matches(rule, laterGiant)).isTrue();
        }

        @Test
        @DisplayName("Leaves out what an exception names now, and keeps reaching later arrivals (\"other creatures they control\")")
        void leavesOutTheExceptionFoundNow() {
            Permanent chosen = addCreatureReady(player2, new GrizzlyBears());
            Permanent other = addCreatureReady(player2, new HillGiant());
            StackEntry entry = entry(player1, player2.getId(), null);
            entry.recordStep(StepKind.CHOOSE, StepOutcome.ofObjects(List.of(chosen.getId()), List.of(player2.getId())));

            PermanentPredicate rule = subjects.lastingRule(gd, entry, EFFECT, (PermanentSubject.Except) except(
                    controlledBy(targetPlayer(), new PermanentIsCreaturePredicate()),
                    Subjects.thisWay(StepKind.CHOOSE)));
            Permanent laterBears = addCreatureReady(player2, new GrizzlyBears());

            assertThat(matches(rule, chosen)).isFalse();
            assertThat(matches(rule, other)).isTrue();
            assertThat(matches(rule, laterBears)).isTrue();
        }

        @Test
        @DisplayName("Is null when the set names no player")
        void namesNoPlayer() {
            StackEntry entry = entry(player1, null, null);

            assertThat(rule(entry, controlledBy(targetPlayer()))).isNull();
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
        @DisplayName("The mode chooser is the opponent the entry recorded as choosing its mode, else no one")
        void modeChooserVariant() {
            StackEntry entry = entry(player1, null, null);
            entry.setModeChooserId(player2.getId());

            assertThat(players(entry, Subjects.modeChooser())).containsExactly(player2.getId());
            assertThat(players(entry(player1, null, null), Subjects.modeChooser())).isEmpty();
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
        @DisplayName("\"That permanent\" of a target ability is its source, from the stack or as it was countered")
        void targetAbilitySourceVariant() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            StackEntry ability = new StackEntry(StackEntryType.ACTIVATED_ABILITY, bears.getCard(), player2.getId(),
                    "ability", List.of(), null, bears.getId());
            gd.stack.add(ability);
            StackEntry entry = entry(player1, ability.getTargetableId(), null);

            assertThat(subjects.permanents(gd, entry, EFFECT, targetAbilitySource())).containsExactly(bears);
            assertThat(subjects.permanents(gd, entry, EFFECT,
                    targetAbilitySource(new PermanentIsArtifactPredicate()))).isEmpty();

            gd.stack.remove(ability);
            assertThat(subjects.permanents(gd, entry, EFFECT, targetAbilitySource())).isEmpty();
            entry.setCounteredAbilitySourceId(bears.getId());
            assertThat(subjects.permanents(gd, entry, EFFECT, targetAbilitySource())).containsExactly(bears);

            Card spellCard = new HillGiant();
            StackEntry spell = new StackEntry(StackEntryType.CREATURE_SPELL, spellCard, player2.getId(),
                    spellCard.getName(), List.of());
            gd.stack.add(spell);
            assertThat(subjects.permanents(gd, entry(player1, spell.getTargetableId(), null), EFFECT,
                    targetAbilitySource())).isEmpty();
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
        @DisplayName("Resolves a set for a source off the battlefield whose controller the caller names (an emblem)")
        void staticSourceWithKnownController() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            addCreatureReady(player1, new HillGiant());
            Permanent standIn = new Permanent(new Forest());

            assertThat(subjects.permanents(gd, standIn, player2.getId(), controlledBy(you()))).containsExactly(bears);
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

    @Nested
    @DisplayName("The static entry point asked about one permanent")
    class StaticMembership {

        private StaticEffectSupport reads;

        @BeforeEach
        void setUpReads() {
            reads = GameTestEngineContext.get().getBean(StaticEffectSupport.class);
        }

        private boolean names(Permanent source, Permanent target, PermanentSubject subject,
                              PermanentPredicate filter) {
            UUID controllerId = gd.findControllerOf(source);
            StaticEffectContext context = new StaticEffectContext(source, target, controllerId,
                    controllerId.equals(gd.findControllerOf(target)), gd);
            return subjects.names(context, subject, filter, reads);
        }

        @Test
        @DisplayName("Names the source and what the source is attached to, nothing else")
        void sourceAndAttached() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent giant = addCreatureReady(player2, new HillGiant());
            Permanent pacifism = attach(player1, new Pacifism(), bears);

            assertThat(names(pacifism, pacifism, source(), null)).isTrue();
            assertThat(names(pacifism, bears, source(), null)).isFalse();
            assertThat(names(pacifism, bears, attached(), null)).isTrue();
            assertThat(names(pacifism, giant, attached(), null)).isFalse();
        }

        @Test
        @DisplayName("Names a set's permanents by whose battlefield they are on, its own predicate and the filter")
        void sets() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent giant = addCreatureReady(player1, new HillGiant());
            Permanent forest = addCreatureReady(player1, new Forest());
            Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());
            PermanentSubject otherCreaturesYouControl = controlledBy(you(),
                    new PermanentAllOfPredicate(List.of(new PermanentNotPredicate(
                            new PermanentIsSourcePermanentPredicate()), new PermanentIsCreaturePredicate())));

            assertThat(names(bears, giant, otherCreaturesYouControl, null)).isTrue();
            assertThat(names(bears, bears, otherCreaturesYouControl, null)).isFalse();
            assertThat(names(bears, forest, otherCreaturesYouControl, null)).isFalse();
            assertThat(names(bears, opposingBears, otherCreaturesYouControl, null)).isFalse();
            assertThat(names(bears, opposingBears, controlledBy(eachOpponent(), new PermanentIsCreaturePredicate()),
                    null)).isTrue();
            assertThat(names(bears, forest, controlledBy(eachPlayer(), new PermanentIsLandPredicate()), null))
                    .isTrue();
            assertThat(names(bears, giant, controlledBy(you()), new PermanentNotPredicate(
                    new PermanentIsSourcePermanentPredicate()))).isTrue();
            assertThat(names(bears, bears, controlledBy(you()), new PermanentNotPredicate(
                    new PermanentIsSourcePermanentPredicate()))).isFalse();
        }

        @Test
        @DisplayName("A tapped-creatures set follows the permanent's tapped state")
        void tapped() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent giant = addCreatureReady(player1, new HillGiant());
            PermanentSubject tappedCreaturesYouControl = controlledBy(you(), new PermanentAllOfPredicate(List.of(
                    new PermanentIsTappedPredicate(), new PermanentIsCreaturePredicate())));

            assertThat(names(bears, giant, tappedCreaturesYouControl, null)).isFalse();
            giant.tap();
            assertThat(names(bears, giant, tappedCreaturesYouControl, null)).isTrue();
        }

        @Test
        @DisplayName("Names the creatures of the player a Curse enchants")
        void enchantedPlayersCreatures() {
            Permanent curse = addCreatureReady(player1, new CurseOfStalkedPrey());
            curse.setAttachedTo(player2.getId());
            Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());
            Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
            PermanentSubject enchantedPlayersCreatures = controlledBy(enchantedPlayer(),
                    new PermanentIsCreaturePredicate());

            assertThat(names(curse, opposingBears, enchantedPlayersCreatures, null)).isTrue();
            assertThat(names(curse, ownBears, enchantedPlayersCreatures, null)).isFalse();
        }

        @Test
        @DisplayName("Agrees with the set the static entry point lists, for every permanent on the battlefield")
        void agreesWithTheList() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            addCreatureReady(player1, new HillGiant()).tap();
            addCreatureReady(player1, new Forest());
            addCreatureReady(player2, new GrizzlyBears());
            addCreatureReady(player2, new Forest());
            List<Permanent> board = gd.playerBattlefields.values().stream().flatMap(List::stream).toList();

            List<PermanentSubject> staticSubjects = List.of(Subjects.source(), Subjects.attached(),
                    Subjects.attachedCreature(), Subjects.sourceAndPaired(), Subjects.creaturesYouControl(),
                    Subjects.otherCreaturesYouControl(), Subjects.otherTappedCreaturesYouControl(),
                    Subjects.otherUntappedCreaturesYouControl(), Subjects.creaturesYourOpponentsControl(),
                    Subjects.permanentsYouControl(), Subjects.otherPermanentsYouControl(), Subjects.allCreatures(),
                    Subjects.otherCreatures(), Subjects.allPermanents(), Subjects.otherPermanents(),
                    Subjects.creaturesEnchantedPlayerControls(), Subjects.landsYouControl(),
                    Subjects.landsYourOpponentsControl(), Subjects.allLands(), Subjects.otherLands());
            for (PermanentSubject subject : staticSubjects) {
                List<Permanent> listed = subjects.permanents(gd, bears, subject);
                for (Permanent permanent : board) {
                    assertThat(names(bears, permanent, subject, null))
                            .as("%s on %s", subject, permanent.getCard().getName())
                            .isEqualTo(listed.contains(permanent));
                }
            }
        }

        @Test
        @DisplayName("Rejects a subject that reads a stack entry")
        void rejectsStackSubjects() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());

            assertThatThrownBy(() -> names(bears, bears, target(), null))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> names(bears, bears, controlledBy(targetPlayer()), null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("The static entry point told whose battlefield the permanent is on")
    class StaticMembershipByBattlefield {

        private StaticEffectSupport reads;

        @BeforeEach
        void setUpReads() {
            reads = GameTestEngineContext.get().getBean(StaticEffectSupport.class);
        }

        @Test
        @DisplayName("Names a creature still entering by the battlefield it is entering")
        void enteringCreature() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent entering = new Permanent(new HillGiant());
            PermanentSubject creaturesYouControl = controlledBy(you(), new PermanentIsCreaturePredicate());

            assertThat(subjects.names(gd, bears, player1.getId(), player1.getId(), entering, creaturesYouControl,
                    reads)).isTrue();
            assertThat(subjects.names(gd, bears, player1.getId(), player2.getId(), entering, creaturesYouControl,
                    reads)).isFalse();
        }

        @Test
        @DisplayName("Names the creatures of the enchanted player by their controller")
        void enchantedPlayersCreatures() {
            Permanent curse = addCreatureReady(player1, new CurseOfStalkedPrey());
            curse.setAttachedTo(player2.getId());
            Permanent entering = new Permanent(new GrizzlyBears());
            PermanentSubject enchantedPlayersCreatures = controlledBy(enchantedPlayer(),
                    new PermanentIsCreaturePredicate());

            assertThat(subjects.names(gd, curse, player1.getId(), player2.getId(), entering,
                    enchantedPlayersCreatures, reads)).isTrue();
            assertThat(subjects.names(gd, curse, player1.getId(), player1.getId(), entering,
                    enchantedPlayersCreatures, reads)).isFalse();
        }

        @Test
        @DisplayName("With no source permanent, names its controller's set and never a source or attachment")
        void noSourcePermanent() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());
            PermanentSubject otherCreaturesYouControl = Subjects.otherCreaturesYouControl();

            assertThat(subjects.names(gd, null, player1.getId(), player1.getId(), bears, otherCreaturesYouControl,
                    reads)).isTrue();
            assertThat(subjects.names(gd, null, player1.getId(), player2.getId(), opposingBears,
                    otherCreaturesYouControl, reads)).isFalse();
            assertThat(subjects.names(gd, null, player1.getId(), player1.getId(), bears, source(), reads))
                    .isFalse();
            assertThat(subjects.names(gd, null, player1.getId(), player1.getId(), bears, attached(), reads))
                    .isFalse();
        }

        @Test
        @DisplayName("A known creature or land answers only that part of a set")
        void knownCharacteristic() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent entering = new Permanent(new HillGiant());
            PermanentSubject creaturesYouControl = controlledBy(you(), new PermanentIsCreaturePredicate());
            PermanentSubject landsYouControl = controlledBy(you(), new PermanentIsLandPredicate());

            assertThat(subjects.names(gd, bears, player1.getId(), player1.getId(), entering, creaturesYouControl,
                    SubjectResolutionService.StaticReads.knownCreature())).isTrue();
            assertThat(subjects.names(gd, bears, player1.getId(), player1.getId(), entering, landsYouControl,
                    SubjectResolutionService.StaticReads.knownCreature())).isFalse();
            assertThat(subjects.names(gd, bears, player1.getId(), player1.getId(), entering, landsYouControl,
                    SubjectResolutionService.StaticReads.knownLand())).isTrue();
            assertThat(subjects.names(gd, bears, player1.getId(), player1.getId(), entering, creaturesYouControl,
                    SubjectResolutionService.StaticReads.knownLand())).isFalse();
        }
    }

    @Nested
    @DisplayName("playersThisWay(kind, filter)")
    class FilteredPlayersThisWay {

        @Test
        @DisplayName("A card filter names the players whose recorded cards match it, wherever the cards went")
        void cardFilterNamesPlayersOfMatchingCards() {
            Card forest = new Forest();
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(forest));
            Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
            StackEntry entry = entry(player1, null, null);
            entry.recordStep(StepKind.DISCARD, StepOutcome.ofObjects(
                    List.of(forest.getId(), giant.getCard().getId(), bears.getId()),
                    List.of(player1.getId(), player2.getId())));
            CardPredicate nonland = new com.github.laxika.magicalvibes.model.filter.CardNotPredicate(
                    new CardTypePredicate(CardType.LAND));

            assertThat(players(entry, Subjects.playersThisWay(StepKind.DISCARD, nonland)))
                    .containsExactly(player2.getId());
            assertThat(players(entry, except(eachPlayer(), Subjects.playersThisWay(StepKind.DISCARD, nonland))))
                    .containsExactly(player1.getId());
        }

        @Test
        @DisplayName("A permanent filter names the controllers of the recorded permanents that match it")
        void permanentFilterNamesControllersOfMatchingPermanents() {
            Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            StackEntry entry = entry(player1, null, null);
            entry.recordStep(StepKind.TAP, StepOutcome.ofObjects(List.of(forest.getId(), bears.getId()),
                    List.of(player1.getId(), player2.getId())));

            assertThat(players(entry, Subjects.playersThisWay(StepKind.TAP, new PermanentIsLandPredicate())))
                    .containsExactly(player1.getId());
            assertThat(players(entry, Subjects.playersThisWay(StepKind.TAP)))
                    .containsExactly(player1.getId(), player2.getId());
        }

        @Test
        @DisplayName("A card filter is refused for a step whose players don't own its objects")
        void cardFilterRefusedForPermanentSteps() {
            assertThatThrownBy(() -> Subjects.playersThisWay(StepKind.SACRIFICE, new CardTypePredicate(CardType.LAND)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("ownedBy names only the cards of the players it names, a card exiled instead of discarded too")
        void ownedByNarrowsToOwners() {
            Card bears = new GrizzlyBears();
            Card giant = new HillGiant();
            Card forest = new Forest();
            harness.setGraveyard(player1, List.of(bears));
            gd.addToExile(player1.getId(), giant);
            harness.setGraveyard(player2, List.of(forest));
            StackEntry entry = entry(player1, null, null);
            entry.recordStep(StepKind.DISCARD, StepOutcome.ofObjects(
                    List.of(bears.getId(), giant.getId(), forest.getId()), List.of(player1.getId(), player2.getId())));

            assertThat(subjects.cards(gd, entry, EFFECT,
                    Subjects.ownedBy(Subjects.cardsThisWay(StepKind.DISCARD), you())))
                    .containsExactly(bears, giant);
            assertThat(subjects.cards(gd, entry, EFFECT,
                    Subjects.ownedBy(Subjects.cardsThisWay(StepKind.DISCARD), eachOpponent())))
                    .containsExactly(forest);
        }
    }

    @Nested
    @DisplayName("Card subjects")
    class CardSubjects {

        @Test
        @DisplayName("The cards an exile step recorded are named while they are in exile, and not once they left")
        void exiledCardsWhileInExile() {
            Card bears = new GrizzlyBears();
            gd.addToExile(player2.getId(), bears);
            StackEntry entry = entry(player1, null, null);
            entry.recordStep(StepKind.EXILE, StepOutcome.ofObjects(List.of(bears.getId()), List.of(player2.getId())));

            assertThat(subjects.cards(gd, entry, EFFECT, Subjects.cardsThisWay(StepKind.EXILE))).containsExactly(bears);

            gd.removeFromExile(bears.getId());
            assertThat(subjects.cards(gd, entry, EFFECT, Subjects.cardsThisWay(StepKind.EXILE))).isEmpty();
        }

        @Test
        @DisplayName("A destroy step names the card its permanent became in the graveyard, and nothing once it moved on")
        void destroyedPermanentsCard() {
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player2, List.of(bears));
            UUID permanentId = UUID.randomUUID();
            StackEntry entry = entry(player1, null, null);
            entry.recordStep(StepKind.DESTROY, StepOutcome.ofObjects(List.of(permanentId), List.of(player2.getId()),
                    List.of(new MovedCard(permanentId, bears.getId(), Zone.GRAVEYARD, player2.getId()))));

            assertThat(subjects.locatedCards(gd, entry, EFFECT, Subjects.cardsThisWay(StepKind.DESTROY)))
                    .containsExactly(new SubjectResolutionService.LocatedCard(bears, Zone.GRAVEYARD, player2.getId()));

            gd.playerGraveyards.get(player2.getId()).remove(bears);
            gd.addToExile(player2.getId(), bears);
            assertThat(subjects.cards(gd, entry, EFFECT, Subjects.cardsThisWay(StepKind.DESTROY))).isEmpty();
        }

        @Test
        @DisplayName("A destroyed token, which became no card, is not named")
        void destroyedTokenNamesNothing() {
            StackEntry entry = entry(player1, null, null);
            entry.recordStep(StepKind.DESTROY, StepOutcome.ofObjects(List.of(UUID.randomUUID()),
                    List.of(player2.getId())));

            assertThat(subjects.cards(gd, entry, EFFECT, Subjects.cardsThisWay(StepKind.DESTROY))).isEmpty();
        }

        @Test
        @DisplayName("A discarded card is named in its graveyard, or in exile where a replacement put it")
        void discardedCard() {
            Card bears = new GrizzlyBears();
            Card giant = new HillGiant();
            harness.setGraveyard(player1, List.of(bears));
            gd.addToExile(player1.getId(), giant);
            StackEntry entry = entry(player1, null, null);
            entry.recordStep(StepKind.DISCARD, StepOutcome.ofObjects(List.of(bears.getId(), giant.getId()),
                    List.of(player1.getId())));

            assertThat(subjects.cards(gd, entry, EFFECT, Subjects.cardsThisWay(StepKind.DISCARD)))
                    .containsExactly(bears, giant);
        }

        @Test
        @DisplayName("A step that moved no card names none, and a kind that moves no card is refused")
        void nothingRecordedAndRefusedKinds() {
            StackEntry entry = entry(player1, null, null);

            assertThat(subjects.cards(gd, entry, EFFECT, Subjects.cardsThisWay(StepKind.EXILE))).isEmpty();
            assertThatThrownBy(() -> Subjects.cardsThisWay(StepKind.DRAW))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("A countered spell's card is named where the counter put it, and nothing once it moved on")
        void counteredSpellsCard() {
            Card bears = new GrizzlyBears();
            gd.addToExile(player2.getId(), bears);
            StackEntry entry = entry(player1, null, null);
            entry.recordStep(StepKind.COUNTER_SPELL, StepOutcome.ofObjects(List.of(bears.getId()),
                    List.of(player2.getId()), List.of(new MovedCard(bears.getId(), bears.getId(), Zone.EXILE,
                            player2.getId()))));

            assertThat(subjects.locatedCards(gd, entry, EFFECT, Subjects.cardsThisWay(StepKind.COUNTER_SPELL)))
                    .containsExactly(new SubjectResolutionService.LocatedCard(bears, Zone.EXILE, player2.getId()));

            gd.removeFromExile(bears.getId());
            assertThat(subjects.cards(gd, entry, EFFECT, Subjects.cardsThisWay(StepKind.COUNTER_SPELL))).isEmpty();
        }

        @Test
        @DisplayName("The topmost matching card of a library is the first match from the top, in that library only")
        void topmostCardInYourLibrary() {
            Card forest = new Forest();
            Card bears = new GrizzlyBears();
            Card giant = new HillGiant();
            harness.setLibrary(player1, List.of(forest, bears, giant));
            harness.setLibrary(player2, List.of(new GrizzlyBears()));
            StackEntry entry = entry(player1, null, null);
            CardPredicate creature = new CardTypePredicate(CardType.CREATURE);

            assertThat(subjects.locatedCards(gd, entry, EFFECT, Subjects.topmostCardInYourLibrary(creature)))
                    .containsExactly(new SubjectResolutionService.LocatedCard(bears, Zone.LIBRARY, player1.getId()));
            assertThat(subjects.cards(gd, entry, EFFECT, Subjects.topmostCardInYourLibrary(null)))
                    .containsExactly(forest);

            harness.setLibrary(player1, List.of(forest));
            assertThat(subjects.cards(gd, entry, EFFECT, Subjects.topmostCardInYourLibrary(creature))).isEmpty();
        }

        @Test
        @DisplayName("The cards an event put into a hand are named while there; a duplicate reads one that left as last known")
        void triggeringCardsInHand() {
            Card bears = new GrizzlyBears();
            Card giant = new HillGiant();
            harness.setHand(player1, List.of(bears, giant));
            StackEntry entry = entry(player1, null, null);
            entry.setTriggeringCards(List.of(bears, giant));

            assertThat(subjects.locatedCards(gd, entry, EFFECT, Subjects.triggeringCardsInHand()))
                    .containsExactly(new SubjectResolutionService.LocatedCard(bears, Zone.HAND, player1.getId()),
                            new SubjectResolutionService.LocatedCard(giant, Zone.HAND, player1.getId()));

            harness.setHand(player1, List.of(giant));
            assertThat(subjects.cards(gd, entry, EFFECT, Subjects.triggeringCardsInHand())).containsExactly(giant);
            assertThat(subjects.cardsToDuplicate(gd, entry, EFFECT, Subjects.triggeringCardsInHand()))
                    .extracting(SubjectResolutionService.LocatedCard::card).containsExactly(bears, giant);
        }

        @Test
        @DisplayName("The cards a player owns are named in every zone, a permanent's as its physical card, no token")
        void cardsYouOwn() {
            Card inLibrary = new GrizzlyBears();
            Card inHand = new GrizzlyBears();
            Card inGraveyard = new GrizzlyBears();
            Card inExile = new GrizzlyBears();
            Card opponents = new GrizzlyBears();
            Card land = new Forest();
            harness.setLibrary(player1, List.of(inLibrary, land));
            harness.setHand(player1, List.of(inHand));
            harness.setGraveyard(player1, List.of(inGraveyard));
            harness.setGraveyard(player2, List.of(opponents));
            gd.addToExile(player1.getId(), inExile);
            Permanent onBattlefield = addCreatureReady(player1, new HillGiant());
            Card tokenCard = new GrizzlyBears();
            tokenCard.setToken(true);
            addCreatureReady(player1, tokenCard);
            StackEntry entry = entry(player1, null, null);
            CardPredicate creature = new CardTypePredicate(CardType.CREATURE);

            assertThat(subjects.locatedCards(gd, entry, EFFECT, Subjects.cardsYouOwn(creature)))
                    .containsExactlyInAnyOrder(
                            new SubjectResolutionService.LocatedCard(inLibrary, Zone.LIBRARY, player1.getId()),
                            new SubjectResolutionService.LocatedCard(inHand, Zone.HAND, player1.getId()),
                            new SubjectResolutionService.LocatedCard(inGraveyard, Zone.GRAVEYARD, player1.getId()),
                            new SubjectResolutionService.LocatedCard(inExile, Zone.EXILE, player1.getId()),
                            new SubjectResolutionService.LocatedCard(onBattlefield.getOriginalCard(), Zone.BATTLEFIELD,
                                    player1.getId()));
        }

        @Test
        @DisplayName("A permanent that is a copy is judged as the copy and named as its own physical card")
        void cardsYouOwnNamesACopysPhysicalCard() {
            harness.setLibrary(player1, List.of());
            harness.setHand(player1, List.of());
            Card forest = new Forest();
            Permanent copying = harness.addToBattlefieldAndReturn(player1, forest);
            copying.setCard(new GrizzlyBears());
            StackEntry entry = entry(player1, null, null);

            assertThat(subjects.locatedCards(gd, entry, EFFECT,
                    Subjects.cardsYouOwn(new CardTypePredicate(CardType.CREATURE))))
                    .containsExactly(new SubjectResolutionService.LocatedCard(forest, Zone.BATTLEFIELD,
                            player1.getId()));
        }

        @Test
        @DisplayName("A target card is named while it is in a graveyard of its scope and matches its filter")
        void targetCard() {
            Card bears = new GrizzlyBears();
            Card forest = new Forest();
            harness.setGraveyard(player1, List.of(bears, forest));
            CardPredicate creature = new CardTypePredicate(CardType.CREATURE);

            StackEntry onBears = entry(player1, bears.getId(), null);
            assertThat(subjects.cards(gd, onBears, EFFECT,
                    Subjects.targetCard(creature, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))).containsExactly(bears);
            assertThat(subjects.cards(gd, onBears, EFFECT,
                    Subjects.targetCard(creature, GraveyardSearchScope.OPPONENT_GRAVEYARD))).isEmpty();

            StackEntry onForest = entry(player1, forest.getId(), null);
            assertThat(subjects.cards(gd, onForest, EFFECT,
                    Subjects.targetCard(creature, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))).isEmpty();
        }

        @Test
        @DisplayName("The cards an activated ability's cost exiled are named while they are in exile")
        void exiledAsCostNamesEveryCostExiledCard() {
            Card bears = new GrizzlyBears();
            Card giant = new HillGiant();
            gd.addToExile(player1.getId(), bears);
            gd.addToExile(player1.getId(), giant);
            StackEntry entry = entry(player1, null, null);
            entry.setActivatedAbilityExiledCardIds(List.of(bears.getId(), giant.getId()));

            assertThat(subjects.cards(gd, entry, EFFECT, Subjects.exiledAsCost())).containsExactly(bears, giant);

            gd.removeFromExile(bears.getId());
            assertThat(subjects.cards(gd, entry, EFFECT, Subjects.exiledAsCost())).containsExactly(giant);
        }

        @Test
        @DisplayName("An exception names a set's permanents but the excluded ones")
        void exceptExcludesPermanents() {
            Permanent source = addCreatureReady(player1, new GrizzlyBears());
            Permanent giant = addCreatureReady(player1, new HillGiant());
            StackEntry entry = entry(player1, null, source.getId());

            assertThat(permanents(entry, except(controlledBy(you(), new PermanentIsCreaturePredicate()), source())))
                    .containsExactly(giant);
            assertThatThrownBy(() -> except(controlledBy(you(), new PermanentIsCreaturePredicate()), target()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("cardsToDuplicate()")
    class CardsToDuplicate {

        @Test
        @DisplayName("A permanent's card, else the card the entry last knew, and never a token")
        void permanentsCardElseLastKnown() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent giant = addCreatureReady(player1, new HillGiant());
            Card tokenCard = new Forest();
            tokenCard.setToken(true);
            Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
            StackEntry entry = entry(player1, null, null);
            entry.setTriggeringPermanentIds(List.of(bears.getId(), giant.getId(), token.getId()));
            entry.rememberLastKnownPermanentCard(giant.getId(), giant.getCard());
            gd.playerBattlefields.get(player1.getId()).remove(giant);

            assertThat(subjects.cardsToDuplicate(gd, entry, EFFECT, triggeringAll()))
                    .containsExactly(bears.getCard(), giant.getCard());
        }

        @Test
        @DisplayName("A trigger event's permanent that has died since is read from the entry's snapshot of it")
        void diedTriggeringPermanentIsReadFromItsSnapshot() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            StackEntry entry = entry(player1, null, null);
            entry.setTriggeringPermanentId(bears.getId());
            entry.setTriggeringPermanent(bears);
            gd.playerBattlefields.get(player1.getId()).remove(bears);

            assertThat(subjects.cardsToDuplicate(gd, entry, EFFECT, triggering())).singleElement()
                    .satisfies(card -> assertThat(card.getName()).isEqualTo("Grizzly Bears"));
        }

        @Test
        @DisplayName("A face-down permanent's card has only its face-down values")
        void faceDownPermanentHasItsFaceDownValues() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            bears.setFaceDown(2, 2, Set.of(CardType.CREATURE));
            StackEntry entry = entry(player1, null, null);
            entry.setTriggeringPermanentIds(List.of(bears.getId()));

            assertThat(subjects.cardsToDuplicate(gd, entry, EFFECT, triggeringAll())).singleElement()
                    .satisfies(card -> {
                        assertThat(card.getName()).isEmpty();
                        assertThat(card.getPower()).isEqualTo(2);
                        assertThat(card.getToughness()).isEqualTo(2);
                        assertThat(card).isNotSameAs(bears.getCard());
                    });
        }
    }

    @Nested
    @DisplayName("mostVoted() / votedFor() / mostVotedCards() / playersWithNoVotes()")
    class Voted {

        private void recordVotes(StackEntry entry, StepOutcome.Vote... votes) {
            entry.recordStep(StepKind.VOTE, StepOutcome.ofVotes(List.of(votes)));
        }

        private StepOutcome.Vote permanentVote(Player voter, Permanent permanent) {
            return new StepOutcome.Vote(voter.getId(), StepOutcome.Vote.PERMANENT + permanent.getId(),
                    permanent.getId());
        }

        @Test
        @DisplayName("mostVoted() is every permanent tied for the most votes, still on the battlefield")
        void mostVotedIsEveryPermanentTiedForMost() {
            Permanent bears = addCreatureReady(player1, new GrizzlyBears());
            Permanent giant = addCreatureReady(player2, new HillGiant());
            Permanent other = addCreatureReady(player2, new GrizzlyBears());
            StackEntry entry = entry(player1, null, null);
            recordVotes(entry, permanentVote(player1, bears), permanentVote(player2, giant));

            assertThat(permanents(entry, Subjects.mostVoted())).containsExactly(bears, giant);

            recordVotes(entry, permanentVote(player1, bears), permanentVote(player2, bears),
                    permanentVote(player2, giant));

            assertThat(permanents(entry, Subjects.mostVoted())).containsExactly(bears);
            assertThat(permanents(entry, Subjects.votedFor())).containsExactly(bears, giant).doesNotContain(other);

            leaveBattlefield(bears);

            assertThat(permanents(entry, Subjects.mostVoted())).isEmpty();
        }

        @Test
        @DisplayName("Nothing has the most votes when no vote was cast")
        void noVotesNamesNothing() {
            addCreatureReady(player1, new GrizzlyBears());
            StackEntry entry = entry(player1, null, null);
            recordVotes(entry);

            assertThat(permanents(entry, Subjects.mostVoted())).isEmpty();
            assertThat(players(entry, Subjects.playersWithNoVotes()))
                    .containsExactly(player1.getId(), player2.getId());
        }

        @Test
        @DisplayName("playersWithNoVotes() is every player in the game no vote named")
        void playersWithNoVotes() {
            StackEntry entry = entry(player1, null, null);
            recordVotes(entry,
                    new StepOutcome.Vote(player1.getId(), StepOutcome.Vote.PLAYER + player2.getId(), player2.getId()),
                    new StepOutcome.Vote(player2.getId(), StepOutcome.Vote.PLAYER + player2.getId(), player2.getId()));

            assertThat(players(entry, Subjects.playersWithNoVotes())).containsExactly(player1.getId());
        }

        @Test
        @DisplayName("mostVotedCards() names the most-voted cards while they are still in the graveyard")
        void mostVotedCardsStayInTheGraveyard() {
            Card bears = new GrizzlyBears();
            Card giant = new HillGiant();
            harness.setGraveyard(player1, List.of(bears, giant));
            StackEntry entry = entry(player1, null, null);
            recordVotes(entry,
                    new StepOutcome.Vote(player1.getId(), StepOutcome.Vote.CARD + bears.getId(), bears.getId()),
                    new StepOutcome.Vote(player2.getId(), StepOutcome.Vote.CARD + bears.getId(), bears.getId()),
                    new StepOutcome.Vote(player2.getId(), StepOutcome.Vote.CARD + giant.getId(), giant.getId()));

            assertThat(subjects.cards(gd, entry, EFFECT, Subjects.mostVotedCards())).containsExactly(bears);

            gd.playerGraveyards.get(player1.getId()).remove(bears);

            assertThat(subjects.cards(gd, entry, EFFECT, Subjects.mostVotedCards())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Static card subjects")
    class StaticCardSubjects {

        private static final SubjectResolutionService.ExiledCardReads TYPE_READS = (card, ownerId, filter) ->
                filter instanceof CardTypePredicate type && card.hasType(type.cardType());

        private List<Card> named(Permanent source, CardSubject cards) {
            return SubjectResolutionService.namedCards(gd, source, player1.getId(), cards, TYPE_READS).stream()
                    .map(SubjectResolutionService.LocatedCard::card)
                    .toList();
        }

        private void linkExile(Card card, String abilityLink) {
            gd.exiledCards.replaceAll(exiled -> exiled.card().getId().equals(card.getId())
                    ? exiled.withAbilityLink(abilityLink) : exiled);
        }

        @Test
        @DisplayName("exiledWithSourceBy names only the cards the linked ability exiled with the source (CR 607.2a)")
        void exiledWithSourceByReadsOneLinkedAbility() {
            Permanent source = addCreatureReady(player1, new GrizzlyBears());
            Card printed = new HillGiant();
            Card gained = new GrizzlyBears();
            Card elsewhere = new RuneclawBear();
            gd.addToExile(player2.getId(), printed, source.getId());
            gd.addToExile(player2.getId(), gained, source.getId());
            gd.addToExile(player2.getId(), elsewhere);
            linkExile(printed, "printed");
            linkExile(gained, "gained:printed");

            assertThat(named(source, Subjects.exiledWithSourceBy("printed", null))).containsExactly(printed);
            assertThat(named(source, Subjects.exiledWithSource())).containsExactlyInAnyOrder(printed, gained);
        }

        @Test
        @DisplayName("A resolving exiledWithSourceBy reads the same link")
        void resolvingExiledWithSourceByReadsTheLink() {
            Permanent source = addCreatureReady(player1, new GrizzlyBears());
            Card printed = new HillGiant();
            Card gained = new GrizzlyBears();
            gd.addToExile(player2.getId(), printed, source.getId());
            gd.addToExile(player2.getId(), gained, source.getId());
            linkExile(printed, "printed");
            StackEntry entry = entry(player1, null, source.getId());

            assertThat(subjects.cards(gd, entry, EFFECT, Subjects.exiledWithSourceBy("printed", null)))
                    .containsExactly(printed);
        }

        @Test
        @DisplayName("cardsInYourGraveyard names the matching cards in the controller's graveyard only")
        void graveyardSetOfTheController() {
            Permanent source = addCreatureReady(player1, new GrizzlyBears());
            Card bears = new GrizzlyBears();
            Card forest = new Forest();
            harness.setGraveyard(player1, List.of(bears, forest));
            harness.setGraveyard(player2, List.of(new HillGiant()));

            assertThat(named(source, Subjects.cardsInYourGraveyard(new CardTypePredicate(CardType.CREATURE))))
                    .containsExactly(bears);
        }

        @Test
        @DisplayName("cardsInGraveyards over each player names the matching cards of every graveyard")
        void graveyardSetOfEachPlayer() {
            Permanent source = addCreatureReady(player1, new GrizzlyBears());
            Card bears = new GrizzlyBears();
            Card giant = new HillGiant();
            harness.setGraveyard(player1, List.of(bears));
            harness.setGraveyard(player2, List.of(giant, new Forest()));

            assertThat(named(source, Subjects.cardsInGraveyards(Subjects.eachPlayer(),
                    new CardTypePredicate(CardType.CREATURE)))).containsExactlyInAnyOrder(bears, giant);
        }

        @Test
        @DisplayName("topmostCardInYourLibrary with no filter names the top card of the controller's library")
        void topCardOfTheControllersLibrary() {
            Permanent source = addCreatureReady(player1, new GrizzlyBears());
            Card forest = new Forest();
            harness.setLibrary(player1, List.of(forest, new GrizzlyBears()));
            harness.setLibrary(player2, List.of(new HillGiant()));

            assertThat(named(source, Subjects.topmostCardInYourLibrary(null))).containsExactly(forest);
        }

        @Test
        @DisplayName("A subject that reads a stack entry has no static form")
        void stackSubjectHasNoStaticForm() {
            Permanent source = addCreatureReady(player1, new GrizzlyBears());

            assertThatThrownBy(() -> named(source, Subjects.cardsThisWay(StepKind.EXILE)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("bindChosenValues")
    class BindChosenValues {

        private PredicateEvaluationService predicates() {
            return GameTestEngineContext.get().getBean(PredicateEvaluationService.class);
        }

        @Test
        @DisplayName("Writes the type this resolution chose into a permanent filter, which keeps it after the resolution")
        void bindsTheTypeChosenThisResolution() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent giant = addCreatureReady(player2, new HillGiant());
            StackEntry entry = entry(player1, null, null);
            entry.setChosenCreatureType(CardSubtype.BEAR);

            PermanentPredicate bound = subjects.bindChosenValues(gd, entry, EFFECT,
                    new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                            new PermanentHasTypeChosenThisResolutionPredicate())));
            gd.stack.remove(entry);

            assertThat(predicates().matchesPermanentPredicate(gd, bears, bound)).isTrue();
            assertThat(predicates().matchesPermanentPredicate(gd, giant, bound)).isFalse();
        }

        @Test
        @DisplayName("A type leaf with nothing chosen matches nothing")
        void unchosenTypeMatchesNothing() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());

            PermanentPredicate bound = subjects.bindChosenValues(gd, entry(player1, null, null), EFFECT,
                    new PermanentHasTypeChosenThisResolutionPredicate());

            assertThat(predicates().matchesPermanentPredicate(gd, bears, bound)).isFalse();
        }

        @Test
        @DisplayName("Binds the players a subject names now: a creature that player gains later matches, another's doesn't")
        void bindsTheChosenPlayer() {
            Permanent yours = addCreatureReady(player1, new GrizzlyBears());
            StackEntry entry = entry(player1, null, null);
            entry.recordStep(StepKind.CHOOSE, StepOutcome.ofAmount(1, List.of(player2.getId())));

            PermanentPredicate bound = subjects.bindChosenValues(gd, entry, EFFECT,
                    new PermanentControlledByPlayersPredicate(Subjects.playersThisWay(StepKind.CHOOSE)));
            Permanent later = addCreatureReady(player2, new HillGiant());

            assertThat(predicates().matchesPermanentPredicate(gd, later, bound)).isTrue();
            assertThat(predicates().matchesPermanentPredicate(gd, yours, bound)).isFalse();
        }

        @Test
        @DisplayName("Reads the source's chosen card type as it last existed once the source has left")
        void bindsTheSourcesChosenCardTypeFromItsSnapshot() {
            Permanent source = addCreatureReady(player1, new GrizzlyBears());
            source.setChosenCardType(CardType.CREATURE);
            StackEntry entry = entry(player1, null, source.getId());
            entry.setSourcePermanentSnapshot(new Permanent(source));
            leaveBattlefield(source);

            CardPredicate bound = subjects.bindChosenValues(gd, entry, EFFECT,
                    new CardHasSourceChosenCardTypePredicate());

            assertThat(predicates().matchesCardPredicate(new HillGiant(), bound, null, gd, player1.getId())).isTrue();
            assertThat(predicates().matchesCardPredicate(new Forest(), bound, null, gd, player1.getId())).isFalse();
        }

        @Test
        @DisplayName("Reads the source's chosen subtype into a card filter")
        void bindsTheSourcesChosenSubtype() {
            Permanent source = addCreatureReady(player1, new GrizzlyBears());
            source.setChosenSubtype(CardSubtype.GIANT);

            CardPredicate bound = subjects.bindChosenValues(gd, entry(player1, null, source.getId()), EFFECT,
                    new CardHasSourceChosenSubtypePredicate(false));

            assertThat(predicates().matchesCardPredicate(new HillGiant(), bound, null, gd, player1.getId())).isTrue();
            assertThat(predicates().matchesCardPredicate(new GrizzlyBears(), bound, null, gd, player1.getId())).isFalse();
        }
    }
}
