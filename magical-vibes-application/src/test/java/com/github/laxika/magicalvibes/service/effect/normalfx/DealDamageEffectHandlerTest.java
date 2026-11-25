package com.github.laxika.magicalvibes.service.effect.normalfx;

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

    private DealDamageEffectHandler handler;

    @Override
    protected void setUpHandler() {
        handler = new DealDamageEffectHandler(damageSupport, gameQueryService, gameOutcomeService,
                amountEvaluationService, conditionEvaluationService, mock(SubjectResolutionService.class),
                mock(GameLogService.class));
    }

    @Test
    void enteringPermanentDamageUsesItsControllerWithoutChangingAbilityOwnership() {
        Permanent creature = addPermanent(player2Id, createCreature("Entering creature", 3, 3));
        StackEntry entry = TestStackDoor.triggered(createCard("Enchantment"), player1Id, "Damage ability", java.util.List.of(),
                player1Id, creature.getId());
        entry.setTriggeringPermanentControllerId(player2Id);
        when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);
        when(gameQueryService.findPermanentController(gd, creature.getId())).thenReturn(player2Id);

        handler.resolve(gd, entry,
                DealDamageEffect.toAnyTarget(new com.github.laxika.magicalvibes.model.amount.Fixed(0)).byTriggeringPermanent());

        verify(damagePreventionService).applyNextInstantOrSorceryDamageRedirectShield(eq(gd), argThat(damageEntry ->
                damageEntry.getControllerId().equals(player2Id)
                        && damageEntry.getEffectiveDamageSourceCard() == creature.getCard()), eq(player1Id), eq(0));
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
}
