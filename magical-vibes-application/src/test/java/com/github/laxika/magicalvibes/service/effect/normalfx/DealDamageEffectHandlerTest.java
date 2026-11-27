package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.rules.DamageDealtEvent;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import org.junit.jupiter.api.BeforeEach;

import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.testutil.TestStackDoor;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.XValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DealDamageEffectHandlerTest extends AbstractDamageHandlerTest {

    @Test
    void recordsActualDamageForFollowingEffectsWithoutCountingEarlierDamage() {
        Card source = createCard("Damage source");
        Permanent creature = addPermanent(player2Id, createCreature("Creature", 4, 4));
        StackEntry entry = createEntry(source, player1Id, creature.getId());
        gd.recordDamageDealtBySource(source.getId(), 5);
        stubDamagePreventable();
        stubDamageFromSourceNotPrevented();
        stubNoDamageMultiplier();
        stubCreatureDamageCore(creature, 4);
        stubNoKeywordsOnSource(entry);
        when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);

        handler.resolve(gd, entry,
                DealDamageEffect.toAnyTarget(2).recordingDamageDealt());

        assertThat(entry.getEventValue()).isEqualTo(2);
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Nested
    @DisplayName("Damage several sources deal at once")
    class DealtAtOnce {

        @Test
        @DisplayName("A creature two sources damage at once is dealt it in one event for itself and one for each source")
        void recipientOfTwoSourcesIsDealtOneRecipientEvent() {
            Card spell = createCard("Brawl");
            Permanent target = addPermanent(player2Id, createCreature("Giant", 3, 3));
            Permanent first = addPermanent(player2Id, createCreature("Bear", 2, 2));
            Permanent second = addPermanent(player2Id, createCreature("Cub", 2, 2));
            StackEntry entry = createEntry(spell, player1Id, target.getId());
            stubDamagePreventable();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(target, 3);
            lenient().when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);

            damageSupport.dealAtOnce(gd, () -> {
                damageSupport.dealCreatureDamage(gd, entry, target, 2, first);
                damageSupport.dealCreatureDamage(gd, entry, target, 2, second);
            });

            assertThat(damageEvents()).filteredOn(DamageDealtEvent.ToPermanent.class::isInstance)
                    .map(DamageDealtEvent.ToPermanent.class::cast)
                    .extracting(DamageDealtEvent.ToPermanent::part, DamageDealtEvent.ToPermanent::damage,
                            DamageDealtEvent.ToPermanent::excessDamage, DamageDealtEvent.ToPermanent::sourceId)
                    .containsExactly(
                            org.assertj.core.groups.Tuple.tuple(DamageDealtEvent.Part.RECIPIENT, 4, 1, null),
                            org.assertj.core.groups.Tuple.tuple(DamageDealtEvent.Part.SOURCE, 2, 0, first.getId()),
                            org.assertj.core.groups.Tuple.tuple(DamageDealtEvent.Part.SOURCE, 2, 1, second.getId()));
            assertThat(target.getMarkedDamage()).isEqualTo(4);
        }

        @Test
        @DisplayName("A recipient only one source damages is dealt it as a whole event")
        void recipientOfOneSourceIsDealtAWholeEvent() {
            Card spell = createCard("Brawl");
            Permanent firstTarget = addPermanent(player2Id, createCreature("Giant", 3, 3));
            Permanent secondTarget = addPermanent(player2Id, createCreature("Ogre", 3, 3));
            Permanent dealer = addPermanent(player2Id, createCreature("Bear", 2, 2));
            StackEntry entry = createEntry(spell, player1Id, firstTarget.getId());
            stubDamagePreventable();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(firstTarget, 3);
            stubCreatureDamageCore(secondTarget, 3);
            lenient().when(gameQueryService.findPermanentById(gd, firstTarget.getId())).thenReturn(firstTarget);
            lenient().when(gameQueryService.findPermanentById(gd, secondTarget.getId())).thenReturn(secondTarget);

            damageSupport.dealAtOnce(gd, () -> {
                damageSupport.dealCreatureDamage(gd, entry, firstTarget, 2, dealer);
                damageSupport.dealCreatureDamage(gd, entry, secondTarget, 2, dealer);
            });

            assertThat(damageEvents()).filteredOn(DamageDealtEvent.ToPermanent.class::isInstance)
                    .map(DamageDealtEvent.ToPermanent.class::cast)
                    .extracting(DamageDealtEvent.ToPermanent::part)
                    .containsExactly(DamageDealtEvent.Part.WHOLE, DamageDealtEvent.Part.WHOLE);
            assertThat(gd.damageDealtAtOnce).isNull();
        }
    }

    private DealDamageEffectHandler handler;

    @Override
    protected void setUpHandler() {
        handler = new DealDamageEffectHandler(damageSupport, gameQueryService, gameOutcomeService,
                amountEvaluationService, conditionEvaluationService, mock(SubjectResolutionService.class),
                mock(PlayerInputService.class));
    }

    @Test
    @DisplayName("The entering permanent deals the damage under its controller; the ability keeps its own controller")
    void enteringPermanentDamageUsesItsControllerWithoutChangingAbilityOwnership() {
        Permanent creature = addPermanent(player2Id, createCreature("Entering creature", 3, 3));
        StackEntry entry = TestStackDoor.triggered(createCard("Enchantment"), player1Id, "Damage ability", java.util.List.of(),
                player1Id, creature.getId());
        entry.setTriggeringPermanentControllerId(player2Id);
        when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);
        when(gameQueryService.findPermanentController(gd, creature.getId())).thenReturn(player2Id);
        stubPlayerDamageCore(player1Id);

        handler.resolve(gd, entry,
                DealDamageEffect.toAnyTarget(new com.github.laxika.magicalvibes.model.amount.Fixed(1)).byTriggeringPermanent());

        verify(replacementOrderService).run(eq(gd), argThat(event ->
                event instanceof com.github.laxika.magicalvibes.model.replacement.DamageEvent damage
                        && damage.recipientPlayerId().equals(player1Id)
                        && damage.entry().getControllerId().equals(player2Id)
                        && damage.entry().getEffectiveDamageSourceCard() == creature.getCard()));
        assertThat(entry.getControllerId()).isEqualTo(player1Id);
    }

    @Test
    void unconditionalRegenerationPreventionAppliesWhenNoDamageIsDealt() {
        Permanent creature = addPermanent(player2Id, createCreature("Creature", 2, 2));
        StackEntry entry = createEntry(createCard("Damage spell"), player1Id, creature.getId());
        when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);
        when(gameQueryService.isCreature(gd, creature)).thenReturn(true);
        DealDamageEffect damage = DealDamageEffect.toAnyTarget(0).cantBeRegenerated();

        handler.resolve(gd, entry, damage);
        assertThat(gd.cantBeRegenerated(creature.getId())).isFalse();

        handler.resolve(gd, entry, damage.withUnconditionalRegenerationPrevention());
        assertThat(gd.cantBeRegenerated(creature.getId())).isTrue();
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
            @DisplayName("Deals lethal damage to a creature and destroys it")
            void dealsLethalDamageToCreatureAndDestroysIt() {
                Card shockCard = createCard("Shock");
                Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                StackEntry entry = createEntry(shockCard, player1Id, bears.getId());
                DealDamageEffect effect = DealDamageEffect.toAnyTarget(2);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(bears, 2);
                stubNoKeywordsOnSource(entry);
                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);

                handler.resolve(gd, entry, effect);

                // Lethal marked damage — the SBA check after resolution performs the destruction.
                assertThat(bears.getMarkedDamage()).isEqualTo(2);
                assertDamageToPermanent(bears, 2, player1Id, shockCard, null);
            }

            @Test
            @DisplayName("Deals non-lethal damage to a creature and it survives")
            void dealsNonLethalDamageToCreature() {
                Card shockCard = createCard("Shock");
                Permanent angel = addPermanent(player2Id, createCreature("Serra Angel", 4, 4));
                StackEntry entry = createEntry(shockCard, player1Id, angel.getId());
                DealDamageEffect effect = DealDamageEffect.toAnyTarget(2);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(angel, 4);
                stubNoKeywordsOnSource(entry);
                when(gameQueryService.findPermanentById(gd, angel.getId())).thenReturn(angel);

                handler.resolve(gd, entry, effect);

                assertThat(angel.getMarkedDamage()).isEqualTo(2);
                verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
                assertDamageToPermanent(angel, 2, player1Id, shockCard, null);
            }

            @Test
            @DisplayName("Deals damage to a player and reduces their life total")
            void dealsDamageToPlayer() {
                Card shockCard = createCard("Shock");
                StackEntry entry = createEntry(shockCard, player1Id, player2Id);
                DealDamageEffect effect = DealDamageEffect.toAnyTarget(2);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);

                handler.resolve(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(18);
                verify(lifeSupport).lifeLost(gd, player2Id, 2);
                assertDamageToPlayer(player2Id, 2);
            }

            @Test
            @DisplayName("Does nothing when targetId is null")
            void doesNothingWhenTargetNull() {
                Card shockCard = createCard("Shock");
                StackEntry entry = createEntry(shockCard, player1Id, null);
                DealDamageEffect effect = DealDamageEffect.toAnyTarget(2);

                handler.resolve(gd, entry, effect);

                            }

            @Test
            @DisplayName("XValue amount deals damage equal to the stack entry's X value")
            void xValueAmountDealsXDamageToCreature() {
                Card blazeCard = createCard("Blaze");
                Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                StackEntry entry = createEntryWithXValue(blazeCard, player1Id, 3, bears.getId());
                DealDamageEffect effect = DealDamageEffect.toAnyTarget(new XValue());

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(bears, 2);
                stubNoKeywordsOnSource(entry);
                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);

                handler.resolve(gd, entry, effect);

                // Lethal marked damage — the SBA check after resolution performs the destruction.
                assertThat(bears.getMarkedDamage()).isEqualTo(3);
                assertDamageToPermanent(bears, 3, player1Id, blazeCard, null);
            }

            @Test
            @DisplayName("XValue amount deals X damage to a player")
            void xValueAmountDealsXDamageToPlayer() {
                Card blazeCard = createCard("Blaze");
                StackEntry entry = createEntryWithXValue(blazeCard, player1Id, 5, player2Id);
                DealDamageEffect effect = DealDamageEffect.toAnyTarget(new XValue());

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);

                handler.resolve(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(15);
                verify(lifeSupport).lifeLost(gd, player2Id, 5);
                assertDamageToPlayer(player2Id, 5);
            }

            @Test
            @DisplayName("Damage is logged via broadcast service")
            void damageIsLogged() {
                Card shockCard = createCard("Shock");
                Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                StackEntry entry = createEntry(shockCard, player1Id, bears.getId());
                DealDamageEffect effect = DealDamageEffect.toAnyTarget(2);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubCreatureDamageCore(bears, 2);
                stubNoKeywordsOnSource(entry);
                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);

                handler.resolve(gd, entry, effect);

                verify(gameLogService, atLeastOnce()).append(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("Shock") && logEntry.plainText().contains("2 damage") && logEntry.plainText().contains("Grizzly Bears")));
            }

    @Nested
    @DisplayName("Target creature")
    class TargetCreature {

        @Test
        @DisplayName("Tracks excess damage for a conditional rider")
        void tracksExcessDamageForConditionalRider() {
            Card burnCard = createCard("Torch the Witness");
            Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, burnCard, player1Id,
                    burnCard.getName(), List.of(new ConditionalEffect(new EventValueAtLeast(1),
                            CreateTokenEffect.ofClueToken(1))));
            entry.setTargetId(bears.getId());
            DealDamageEffect effect = DealDamageEffect.toTargetCreature(3);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(bears, 2);
            stubNoKeywordsOnSource(entry);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);

            handler.resolve(gd, entry, effect);

            assertThat(entry.getEventValue()).isEqualTo(1);
        }

        @Test
                @DisplayName("Deals lethal damage to a creature — marked for the SBA check")
                void dealsDamageToCreature() {
                    Card burnCard = createCard("Burn the Impure");
                    Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                    StackEntry entry = createEntry(burnCard, player1Id, bears.getId());
                    DealDamageEffect effect = DealDamageEffect.toTargetCreature(3);

                    stubDamagePreventable();
                    stubDamageFromSourceNotPrevented();
                    stubNoDamageMultiplier();
                    stubCreatureDamageCore(bears, 2);
                    stubNoKeywordsOnSource(entry);
                    when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);

                    handler.resolve(gd, entry, effect);

                    // Lethal marked damage — the SBA check after resolution performs the destruction.
                    assertThat(bears.getMarkedDamage()).isEqualTo(3);
                    assertDamageToPermanent(bears, 3, player1Id, burnCard, null);
                }

                @Test
                @DisplayName("Tracks creature in permanentsDealtDamageThisTurn when damage is dealt")
                void tracksPermanentDealtDamageThisTurn() {
                    Card burnCard = createCard("Shock");
                    Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                    StackEntry entry = createEntry(burnCard, player1Id, bears.getId());
                    DealDamageEffect effect = DealDamageEffect.toTargetCreature(1);

                    stubDamagePreventable();
                    stubDamageFromSourceNotPrevented();
                    stubNoDamageMultiplier();
                    stubCreatureDamageCore(bears, 2);
                    stubNoKeywordsOnSource(entry);
                    when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);

                    handler.resolve(gd, entry, effect);

                    assertThat(gd.permanentsDealtDamageThisTurn).contains(bears.getId());
                }

                @Test
                @DisplayName("Multi-target: deals damage to each creature in targetIds")
                void multiTargetDealsDamageToEachCreature() {
                    Card burnCard = createCard("Dual Shot");
                    Permanent bear1 = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                    Permanent bear2 = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                    StackEntry entry = createMultiTargetEntry(burnCard, player1Id, List.of(bear1.getId(), bear2.getId()));
                    DealDamageEffect effect = DealDamageEffect.toTargetCreature(1);

                    stubDamagePreventable();
                    stubDamageFromSourceNotPrevented();
                    stubNoDamageMultiplier();
                    stubCreatureDamageCore(bear1, 2);
                    stubCreatureDamageCore(bear2, 2);
                    stubNoKeywordsOnSource(entry);
                    when(gameQueryService.findPermanentById(gd, bear1.getId())).thenReturn(bear1);
                    when(gameQueryService.findPermanentById(gd, bear2.getId())).thenReturn(bear2);

                    handler.resolve(gd, entry, effect);

                    assertThat(bear1.getMarkedDamage()).isEqualTo(1);
                    assertThat(bear2.getMarkedDamage()).isEqualTo(1);
                }

                @Test
                @DisplayName("Multi-target: skips removed targets and damages remaining ones")
                void multiTargetSkipsRemovedTargets() {
                    Card burnCard = createCard("Dual Shot");
                    Permanent bear1 = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                    Permanent bear2 = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                    UUID removedId = bear1.getId();
                    StackEntry entry = createMultiTargetEntry(burnCard, player1Id, List.of(removedId, bear2.getId()));
                    DealDamageEffect effect = DealDamageEffect.toTargetCreature(1);

                    stubDamagePreventable();
                    stubDamageFromSourceNotPrevented();
                    stubNoDamageMultiplier();
                    stubCreatureDamageCore(bear2, 2);
                    stubNoKeywordsOnSource(entry);
                    // bear1 was removed from battlefield before resolution
                    when(gameQueryService.findPermanentById(gd, removedId)).thenReturn(null);
                    when(gameQueryService.findPermanentById(gd, bear2.getId())).thenReturn(bear2);

                    handler.resolve(gd, entry, effect);

                    assertThat(bear2.getMarkedDamage()).isEqualTo(1);
                }

                @Test
                @DisplayName("Single targetIds entry with primary targetId uses single-target path")
                void singleTargetIdsWithPrimaryTargetUsesSingleTargetPath() {
                    Card burnCard = createCard("Goblin Barrage");
                    Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
                    // Kicked spell: creature in targetId, player in targetIds (size 1)
                    StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, burnCard, player1Id, burnCard.getName(),
                            List.of(), 0, bears.getId(), null, Map.of(), null, List.of(), List.of(player2Id));
                    DealDamageEffect effect = DealDamageEffect.toTargetCreature(4);

                    stubDamagePreventable();
                    stubDamageFromSourceNotPrevented();
                    stubNoDamageMultiplier();
                    stubCreatureDamageCore(bears, 2);
                    stubNoKeywordsOnSource(entry);
                    when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);

                    handler.resolve(gd, entry, effect);

                    assertThat(bears.getMarkedDamage()).isEqualTo(4);
                }
    }

    /** Player recipients ({@code DealDamageEffect.to(PlayerSubject, ...)}), with a real subject resolver. */
    @Nested
    @DisplayName("Player recipients")
    class Players {


        private DealDamageEffectHandler handler;

        @Test
        @DisplayName("Deals to the triggering permanent's current controller")
        void damageUsesTriggeringPermanentsCurrentController() {
            Card source = createCard("Damage source");
            Permanent land = new Permanent(createCard("Land"));
            StackEntry entry = createEntry(source, player1Id, player1Id);
            entry.setTriggeringPermanentId(land.getId());
            entry.setTriggeringPermanentControllerId(player1Id);
            when(gameQueryService.findPermanentController(gd, land.getId())).thenReturn(player2Id);
            when(gameQueryService.findTriggeringPermanentController(gd, entry)).thenCallRealMethod();
            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubPlayerDamageCore(player2Id);
            stubNoInfectOnSource(entry);

            handler.resolve(gd, entry, DealDamageEffect.to(Subjects.controllerOf(Subjects.triggering()), 2));

            assertThat(gd.getLife(player1Id)).isEqualTo(20);
            assertThat(gd.getLife(player2Id)).isEqualTo(18);
        }

        @Test
        @DisplayName("Records the damage actually dealt for a following effect")
        void recordsActualDamageForFollowingEffect() {
            StackEntry entry = createEntry(createCard("Damage source"), player1Id, player2Id);
            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubPlayerDamageCore(player2Id);
            stubNoInfectOnSource(entry);

            handler.resolve(gd, entry,
                    DealDamageEffect.to(Subjects.eachOpponent(), 2).recordingDamageDealt());

            assertThat(entry.getEventValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("Deals to the triggering permanent's last-known controller once it has left")
        void damageUsesLastKnownControllerWhenTriggeringPermanentHasLeft() {
            StackEntry entry = createEntry(createCard("Damage source"), player1Id, player1Id);
            UUID landId = UUID.randomUUID();
            entry.setTriggeringPermanentId(landId);
            entry.setTriggeringPermanentControllerId(player1Id);
            entry.getRemovedPermanentControllers().put(landId, player2Id);
            when(gameQueryService.findTriggeringPermanentController(gd, entry)).thenCallRealMethod();
            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubPlayerDamageCore(player2Id);
            stubNoInfectOnSource(entry);

            handler.resolve(gd, entry, DealDamageEffect.to(Subjects.controllerOf(Subjects.triggering()), 2));

            assertThat(gd.getLife(player1Id)).isEqualTo(20);
            assertThat(gd.getLife(player2Id)).isEqualTo(18);
        }

        @Test
        @DisplayName("Accepted opponents take no damage when no player accepted")
        void selectedOpponentsTakeNoDamageWhenNoPlayerAccepted() {
            StackEntry entry = createEntry(createCard("Snort"), player1Id, null);

            handler.resolve(gd, entry, DealDamageEffect.to(Subjects.except(Subjects.acceptedPlayers(), Subjects.you()), 5));

            assertThat(gd.getLife(player1Id)).isEqualTo(20);
            assertThat(gd.getLife(player2Id)).isEqualTo(20);
        }

        @BeforeEach
        void setUpPlayersHandler() {
            handler = new DealDamageEffectHandler(damageSupport, gameQueryService, gameOutcomeService,
                    amountEvaluationService, conditionEvaluationService,
                    new SubjectResolutionService(gameQueryService, predicateEvaluationService,
                            mock(com.github.laxika.magicalvibes.service.target.TargetLegalityService.class)),
                    mock(PlayerInputService.class));
        }

        @Nested
        @DisplayName("targetPlayer() subject")
        class TargetPlayer {

            @Test
            @DisplayName("Deals damage to target player")
            void dealsDamageToTargetPlayer() {
                Card lavaAxeCard = createCard("Lava Axe");
                StackEntry entry = createEntry(lavaAxeCard, player1Id, player2Id);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);

                handler.resolve(gd, entry, DealDamageEffect.to(Subjects.targetPlayer(), 5));

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(15);
                verify(lifeSupport).lifeLost(gd, player2Id, 5);
                assertDamageToPlayer(player2Id, 5);
            }

            @Test
            @DisplayName("Does nothing when target is not a player")
            void doesNothingWhenTargetNotPlayer() {
                Card lavaAxeCard = createCard("Lava Axe");
                UUID fakeId = UUID.randomUUID();
                StackEntry entry = createEntry(lavaAxeCard, player1Id, fakeId);

                handler.resolve(gd, entry, DealDamageEffect.to(Subjects.targetPlayer(), 5));

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
                        }

            @Test
            @DisplayName("Deals damage equal to target player's hand size (CardsInHand(TARGET_PLAYER))")
            void dealsDamageEqualToHandSize() {
                Card impactCard = createCard("Sudden Impact");
                StackEntry entry = createEntry(impactCard, player1Id, player2Id);
                for (int i = 0; i < 5; i++) {
                    gd.playerHands.get(player2Id).add(createCreature("Bear " + i, 2, 2));
                }

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);

                handler.resolve(gd, entry,
                        DealDamageEffect.to(Subjects.targetPlayer(), new CardsInHand(CountScope.TARGET_PLAYER)));

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(15);
                verify(lifeSupport).lifeLost(gd, player2Id, 5);
            }

            @Test
            @DisplayName("Deals 0 damage when target has an empty hand")
            void dealsZeroDamageWhenEmptyHand() {
                Card impactCard = createCard("Sudden Impact");
                StackEntry entry = createEntry(impactCard, player1Id, player2Id);

                stubDamageFromSourceNotPrevented();

                handler.resolve(gd, entry,
                        DealDamageEffect.to(Subjects.targetPlayer(), new CardsInHand(CountScope.TARGET_PLAYER)));

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
                        }
        }

        @Nested
        @DisplayName("you() subject")
        class Controller {

            @Test
            @DisplayName("Deals damage to the controller of the ability")
            void dealsDamageToController() {
                Card artilleryCard = createCard("Orcish Artillery");
                StackEntry entry = createEntry(artilleryCard, player1Id, null);

                stubNoDamageMultiplier();
                stubDamageFromSourceNotPrevented();
                stubPlayerDamageCore(player1Id);
                stubNoInfectOnSource(entry);

                handler.resolve(gd, entry, DealDamageEffect.to(Subjects.you(), 3));

                assertThat(gd.playerLifeTotals.get(player1Id)).isEqualTo(17);
                verify(lifeSupport).lifeLost(gd, player1Id, 3);
                assertDamageToPlayer(player1Id, 3);
            }
        }

        @Nested
        @DisplayName("defendingPlayer() subject")
        class DefendingPlayer {

            @Test
            @DisplayName("Deals damage to the player attacked by the source")
            void dealsDamageToDefendingPlayer() {
                Card sourceCard = createCard("Ghost-Spider, Gwen Stacy");
                StackEntry entry = createEntry(sourceCard, player1Id, null);
                entry.setAttackedTargetId(player2Id);
                when(gameQueryService.defendingPlayerOfAttackTarget(gd, player2Id)).thenReturn(player2Id);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);

                handler.resolve(gd, entry, DealDamageEffect.to(Subjects.defendingPlayer(), 3));

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(17);
                assertThat(gd.playerLifeTotals.get(player1Id)).isEqualTo(20);
            }

            @Test
            @DisplayName("Deals damage to a planeswalker's controller rather than the planeswalker")
            void dealsDamageToAttackedPlaneswalkersController() {
                Card sourceCard = createCard("Ghost-Spider, Gwen Stacy");
                Permanent planeswalker = addPermanent(player2Id, createCard("Jace Beleren"));
                StackEntry entry = createEntry(sourceCard, player1Id, null);
                entry.setAttackedTargetId(planeswalker.getId());
                when(gameQueryService.defendingPlayerOfAttackTarget(gd, planeswalker.getId())).thenReturn(player2Id);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);

                handler.resolve(gd, entry, DealDamageEffect.to(Subjects.defendingPlayer(), 3));

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(17);
            }
        }

        @Nested
        @DisplayName("controllerOf(target()) subject")
        class TargetPermanentController {

            @Test
            void usesTheControllerRememberedBeforeAnEarlierEffectDestroyedTheTarget() {
                UUID removedId = UUID.randomUUID();
                StackEntry entry = createEntry(createCard("Destruction and damage"), player1Id, removedId);
                entry.getRemovedPermanentControllers().put(removedId, player2Id);
                stubNoDamageMultiplier();
                stubDamageFromSourceNotPrevented();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);

                handler.resolve(gd, entry, DealDamageEffect.to(Subjects.controllerOf(Subjects.target()), 2));

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(18);
                assertThat(gd.playerLifeTotals.get(player1Id)).isEqualTo(20);
            }

            @Test
            @DisplayName("Deals to the targeted creature's controller as the victim, without remapping the damage source")
            void dealsToTargetCreatureControllerAndKeepsSourceController() {
                // Chandra's Outrage: "deals 2 damage to that creature's controller."
                Card outrageCard = createCard("Chandra's Outrage");
                Permanent targetCreature = addPermanent(player2Id, createCreature("Bear", 2, 2));
                StackEntry entry = createEntry(outrageCard, player1Id, targetCreature.getId());

                when(gameQueryService.findPermanentById(gd, targetCreature.getId())).thenReturn(targetCreature);
                when(gameQueryService.findPermanentController(gd, targetCreature.getId())).thenReturn(player2Id);
                stubNoDamageMultiplier();
                stubDamageFromSourceNotPrevented();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);

                handler.resolve(gd, entry, DealDamageEffect.to(Subjects.controllerOf(Subjects.target()), 2));

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(18);
                // The victim is player2, but the damage SOURCE controller stays the caster (player1) so
                // opponent-source damage-reduction shields keep applying — entry.controllerId is never remapped.
                assertThat(damageEvents()).anySatisfy(event -> assertThat(event)
                        .isInstanceOfSatisfying(DamageDealtEvent.ToPlayer.class, dealt -> {
                            assertThat(dealt.playerId()).isEqualTo(player2Id);
                            assertThat(dealt.sourceControllerId()).isEqualTo(player1Id);
                        }));
                assertThat(entry.getControllerId()).isEqualTo(player1Id);
            }
        }

        @Nested
        @DisplayName("controllerOf(targetSpell()) subject")
        class TargetSpellController {

            @Test
            @DisplayName("Deals TargetSpellManaValue damage to the targeted spell's controller")
            void dealsManaValueDamageToSpellController() {
                Card refuseCard = createCard("Refuse");
                Card targetSpellCard = createCard("Counsel of the Soratami");
                // Printed mana value is left at 0 by createCard; set via xValue on the stack entry
                // plus a non-zero printed MV isn't available here — use Fixed amount for the recipient path.
                StackEntry targetSpell = createEntry(targetSpellCard, player2Id, null);
                gd.stack.add(targetSpell);

                StackEntry entry = createEntry(refuseCard, player1Id, targetSpellCard.getId());

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);

                handler.resolve(gd, entry, DealDamageEffect.to(Subjects.controllerOf(Subjects.targetSpell()), 4));

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(16);
            }

            @Test
            @DisplayName("Does nothing when the targeted spell has left the stack")
            void doesNothingWhenSpellLeftStack() {
                Card refuseCard = createCard("Refuse");
                UUID goneSpellId = UUID.randomUUID();
                StackEntry entry = createEntry(refuseCard, player1Id, goneSpellId);

                handler.resolve(gd, entry, DealDamageEffect.to(Subjects.controllerOf(Subjects.targetSpell()), 4));

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
                        }
        }

        @Nested
        @DisplayName("eachOpponent() subject")
        class EachOpponent {

            @Test
            @DisplayName("Deals damage to each opponent but not the controller")
            void dealsToEachOpponent() {
                Card paladinCard = createCard("Cabal Paladin");
                StackEntry entry = createEntry(paladinCard, player1Id, null);

                stubDamagePreventable();
                stubDamageFromSourceNotPrevented();
                stubNoDamageMultiplier();
                stubPlayerDamageCore(player2Id);
                stubNoInfectOnSource(entry);

                handler.resolve(gd, entry, DealDamageEffect.to(Subjects.eachOpponent(), 2));

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(18);
                assertThat(gd.playerLifeTotals.get(player1Id)).isEqualTo(20);
            }
        }
    }

    @Nested
    @DisplayName("Mass damage to each creature, planeswalker or battle and each player")
    class Mass {

        private DealDamageEffectHandler handler;

        @BeforeEach
        void setUpMassHandler() {
            handler = new DealDamageEffectHandler(damageSupport, gameQueryService, gameOutcomeService,
                    amountEvaluationService, conditionEvaluationService,
                    new SubjectResolutionService(gameQueryService, predicateEvaluationService,
                            mock(com.github.laxika.magicalvibes.service.target.TargetLegalityService.class)),
                    mock(PlayerInputService.class));
            // The set's filter ("each creature") admits every permanent of these tests.
            lenient().when(predicateEvaluationService.matchesPermanentPredicate(any(Permanent.class), any(), any()))
                    .thenReturn(true);
        }

        @Test
        @DisplayName("Deals damage to all creatures; lethal amounts are marked for the state-based check")
        void damagesAllCreatures() {
            Card pyroCard = createCard("Pyroclasm");
            Permanent bears = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2));
            Permanent elves = addPermanent(player2Id, createCreature("Llanowar Elves", 1, 1));
            StackEntry entry = createEntry(pyroCard, player1Id, null);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            when(gameQueryService.isCreature(eq(gd), any(Permanent.class))).thenReturn(true);
            when(gameQueryService.findPermanentController(eq(gd), eq(bears.getId()))).thenReturn(player1Id);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            when(gameQueryService.findPermanentController(eq(gd), eq(elves.getId()))).thenReturn(player2Id);
            when(gameQueryService.findPermanentById(gd, elves.getId())).thenReturn(elves);
            stubNoKeywordsOnSource(entry);

            handler.resolve(gd, entry, DealDamageEffect.toEachCreature(2));

            assertThat(bears.getMarkedDamage()).isEqualTo(2);
            assertThat(elves.getMarkedDamage()).isEqualTo(2);
            assertDamageToPermanent(bears, 2, player1Id, pyroCard, null);
            assertDamageToPermanent(elves, 2, player1Id, pyroCard, null);
        }

        @Test
        @DisplayName("Does not destroy a creature whose toughness is greater than the damage")
        void doesNotKillHighToughnessCreatures() {
            Card pyroCard = createCard("Pyroclasm");
            Permanent angel = addPermanent(player1Id, createCreature("Serra Angel", 4, 4));
            StackEntry entry = createEntry(pyroCard, player1Id, null);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            when(gameQueryService.isCreature(eq(gd), any(Permanent.class))).thenReturn(true);
            when(gameQueryService.findPermanentController(eq(gd), eq(angel.getId()))).thenReturn(player1Id);
            when(gameQueryService.findPermanentById(gd, angel.getId())).thenReturn(angel);
            stubNoKeywordsOnSource(entry);

            handler.resolve(gd, entry, DealDamageEffect.toEachCreature(2));

            assertThat(angel.getMarkedDamage()).isEqualTo(2);
            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
            assertDamageToPermanent(angel, 2, player1Id, pyroCard, null);
        }

        @Test
        @DisplayName("Deals X damage to each creature and each player in one resolution")
        void dealsXDamageToCreaturesAndPlayers() {
            Card hurricaneCard = createCard("Hurricane");
            hurricaneCard.setColor(com.github.laxika.magicalvibes.model.CardColor.GREEN);
            Permanent angel = addPermanent(player2Id, createCreature("Serra Angel", 4, 4));
            Permanent bears = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2));
            StackEntry entry = createEntryWithXValue(hurricaneCard, player1Id, 4, null);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            when(gameQueryService.isCreature(eq(gd), any(Permanent.class))).thenReturn(true);
            when(gameQueryService.findPermanentController(eq(gd), eq(angel.getId()))).thenReturn(player2Id);
            when(gameQueryService.findPermanentById(gd, angel.getId())).thenReturn(angel);
            when(gameQueryService.findPermanentController(eq(gd), eq(bears.getId()))).thenReturn(player1Id);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            stubNoKeywordsOnSource(entry);
            stubPlayerDamageCore(player1Id);
            stubPlayerDamageCore(player2Id);

            handler.resolve(gd, entry, DealDamageEffect.toEachCreature(new XValue()).andTo(Subjects.eachPlayer()));

            assertThat(gd.playerLifeTotals.get(player1Id)).isEqualTo(16);
            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(16);
            verify(gameOutcomeService).checkWinCondition(gd);
            assertDamageToPermanent(angel, 4, player1Id, hurricaneCard, null);
            assertDamageToPermanent(bears, 4, player1Id, hurricaneCard, null);
            verify(lifeSupport).lifeLost(gd, player1Id, 4);
            verify(lifeSupport).lifeLost(gd, player2Id, 4);
            assertDamageToPlayer(player1Id, 4);
            assertDamageToPlayer(player2Id, 4);
        }

        @Test
        @DisplayName("Removes defense counters from a battle in the set")
        void damagesBattles() {
            Card intoTheFireCard = createCard("Into the Fire");
            Card battleCard = createCard("Battle");
            battleCard.setType(com.github.laxika.magicalvibes.model.CardType.BATTLE);
            Permanent battle = addPermanent(player1Id, battleCard);
            battle.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.DEFENSE, 5);
            StackEntry entry = createEntry(intoTheFireCard, player1Id, null);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            when(gameQueryService.isCreature(eq(gd), any(Permanent.class))).thenReturn(false);
            when(gameQueryService.findPermanentController(eq(gd), eq(battle.getId()))).thenReturn(player1Id);
            when(gameQueryService.findPermanentById(gd, battle.getId())).thenReturn(battle);
            stubNoKeywordsOnSource(entry);

            handler.resolve(gd, entry, DealDamageEffect.toEach(Subjects.allPermanents(), 2));

            assertThat(battle.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.DEFENSE)).isEqualTo(3);
            verify(battleDefeatSupport).checkAfterDefenseRemoved(gd, battle);
        }
    }
}
