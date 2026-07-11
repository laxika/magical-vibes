package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreatureControlServiceTest {

    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private GameQueryService gameQueryService;

    @InjectMocks private CreatureControlService creatureControlService;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));

        lenient().when(gameQueryService.findPermanentById(eq(gd), any(UUID.class)))
                .thenAnswer(inv -> {
                    UUID id = inv.getArgument(1);
                    for (UUID pid : gd.orderedPlayerIds) {
                        for (Permanent p : gd.playerBattlefields.get(pid)) {
                            if (p.getId().equals(id)) return p;
                        }
                    }
                    return null;
                });
    }

    private Card createCreatureCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private Permanent addCreature(UUID playerId, String name) {
        Card card = createCreatureCard(name);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(playerId).add(permanent);
        return permanent;
    }

    private void applySteal(UUID newControllerId, Permanent target, EffectDuration duration,
                            UUID sourcePermanentId) {
        ControlDuration controlDuration = switch (duration) {
            case UNTIL_END_OF_TURN -> ControlDuration.END_OF_TURN;
            case WHILE_SOURCE_ON_BATTLEFIELD -> ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD;
            default -> ControlDuration.PERMANENT;
        };
        creatureControlService.applyControlEffect(gd, newControllerId, target,
                new GainControlOfTargetEffect(controlDuration), duration, sourcePermanentId, "Test Source");
    }

    @Nested
    @DisplayName("applyControlEffect")
    class ApplyControlEffect {

        @Test
        @DisplayName("Moves creature to the new controller's battlefield")
        void movesCreatureBetweenBattlefields() {
            Permanent bear = addCreature(player1Id, "Grizzly Bears");

            applySteal(player2Id, bear, EffectDuration.UNTIL_END_OF_TURN, null);

            assertThat(gd.playerBattlefields.get(player1Id))
                    .noneMatch(p -> p.getId().equals(bear.getId()));
            assertThat(gd.playerBattlefields.get(player2Id))
                    .anyMatch(p -> p.getId().equals(bear.getId()));
        }

        @Test
        @DisplayName("Sets creature as summoning sick after stealing (CR 302.6)")
        void setsSummoningSick() {
            Permanent bear = addCreature(player1Id, "Grizzly Bears");
            assertThat(bear.isSummoningSick()).isFalse();

            applySteal(player2Id, bear, EffectDuration.UNTIL_END_OF_TURN, null);

            assertThat(bear.isSummoningSick()).isTrue();
        }

        @Test
        @DisplayName("Moving between battlefields keeps the CR 613.7 timestamp")
        void keepsTimestampOnControlChange() {
            Permanent bear = addCreature(player1Id, "Grizzly Bears");
            long stamp = bear.getTimestamp();

            applySteal(player2Id, bear, EffectDuration.UNTIL_END_OF_TURN, null);

            assertThat(bear.getTimestamp()).isEqualTo(stamp);
        }

        @Test
        @DisplayName("Records original owner in stolenCreatures map")
        void recordsOriginalOwner() {
            Permanent bear = addCreature(player1Id, "Grizzly Bears");

            applySteal(player2Id, bear, EffectDuration.PERMANENT, null);

            assertThat(gd.stolenCreatures).containsEntry(bear.getId(), player1Id);
        }

        @Test
        @DisplayName("Stealing an already-stolen creature keeps the original owner record")
        void doesNotOverwriteOriginalOwnerOnSecondSteal() {
            Permanent bear = addCreature(player1Id, "Grizzly Bears");

            applySteal(player2Id, bear, EffectDuration.PERMANENT, null);
            applySteal(player2Id, bear, EffectDuration.PERMANENT, null);

            assertThat(gd.stolenCreatures).containsEntry(bear.getId(), player1Id);
        }

        @Test
        @DisplayName("Effect on a creature the player already controls causes no move and no log")
        void doesNothingWhenAlreadyControlled() {
            Permanent bear = addCreature(player1Id, "Grizzly Bears");

            applySteal(player1Id, bear, EffectDuration.UNTIL_END_OF_TURN, null);

            assertThat(gd.playerBattlefields.get(player1Id))
                    .anyMatch(p -> p.getId().equals(bear.getId()));
            assertThat(bear.isSummoningSick()).isFalse();
            assertThat(gd.stolenCreatures).isEmpty();
            verify(gameBroadcastService, never()).logAndBroadcast(any(), anyString());
        }

        @Test
        @DisplayName("Logs the control change to the game log")
        void logsControlChange() {
            Permanent bear = addCreature(player1Id, "Grizzly Bears");

            applySteal(player2Id, bear, EffectDuration.UNTIL_END_OF_TURN, null);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Player2 gains control of Grizzly Bears."));
        }

        @Test
        @DisplayName("The newest control effect wins over an earlier one (CR 613.7)")
        void latestEffectWins() {
            Permanent bear = addCreature(player2Id, "Grizzly Bears");

            applySteal(player1Id, bear, EffectDuration.PERMANENT, null);
            assertThat(gd.playerBattlefields.get(player1Id)).contains(bear);

            applySteal(player2Id, bear, EffectDuration.UNTIL_END_OF_TURN, null);

            assertThat(gd.playerBattlefields.get(player2Id)).contains(bear);
        }
    }

    @Nested
    @DisplayName("reconcileControl")
    class ReconcileControl {

        @Test
        @DisplayName("Expired until-end-of-turn effect reverts the creature to its owner")
        void expiredStealRevertsToOwner() {
            Permanent bear = addCreature(player2Id, "Grizzly Bears");
            applySteal(player1Id, bear, EffectDuration.UNTIL_END_OF_TURN, null);

            gd.expireEndOfTurnFloatingEffects();
            creatureControlService.reconcileControl(gd);

            assertThat(gd.playerBattlefields.get(player2Id)).contains(bear);
            assertThat(gd.stolenCreatures).doesNotContainKey(bear.getId());
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Grizzly Bears returns to Player2's control."));
        }

        @Test
        @DisplayName("Expired temporary steal falls back to a still-active earlier control effect, not the owner")
        void expiredStealFallsBackToEarlierEffect() {
            Permanent bear = addCreature(player2Id, "Grizzly Bears");
            Permanent sower = addCreature(player1Id, "Sower of Temptation");
            applySteal(player1Id, bear, EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD, sower.getId());
            applySteal(player2Id, bear, EffectDuration.UNTIL_END_OF_TURN, null);
            assertThat(gd.playerBattlefields.get(player2Id)).contains(bear);

            gd.expireEndOfTurnFloatingEffects();
            creatureControlService.reconcileControl(gd);

            assertThat(gd.playerBattlefields.get(player1Id)).contains(bear);
            assertThat(gd.stolenCreatures).containsEntry(bear.getId(), player2Id);
        }

        @Test
        @DisplayName("Source leaving the battlefield ends its effect; a later effect keeps control")
        void sourceDeathLeavesLaterEffectActive() {
            Permanent bear = addCreature(player2Id, "Grizzly Bears");
            Permanent sower = addCreature(player1Id, "Sower of Temptation");
            applySteal(player1Id, bear, EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD, sower.getId());
            applySteal(player1Id, bear, EffectDuration.UNTIL_END_OF_TURN, null);

            gd.playerBattlefields.get(player1Id).remove(sower);
            gd.expireFloatingEffectsForDepartedSource(sower.getId());
            creatureControlService.reconcileControl(gd);

            assertThat(gd.playerBattlefields.get(player1Id)).contains(bear);
        }

        @Test
        @DisplayName("Expires a while-source effect when its creator no longer controls the source")
        void expiresEffectWhenSourceChangesController() {
            Permanent bear = addCreature(player2Id, "Grizzly Bears");
            Permanent olivia = addCreature(player1Id, "Olivia Voldaren");
            applySteal(player1Id, bear, EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD, olivia.getId());
            assertThat(gd.playerBattlefields.get(player1Id)).contains(bear);

            gd.playerBattlefields.get(player1Id).remove(olivia);
            gd.playerBattlefields.get(player2Id).add(olivia);
            creatureControlService.reconcileControl(gd);

            assertThat(gd.playerBattlefields.get(player2Id)).contains(bear);
            assertThat(gd.floatingEffects).noneMatch(fe -> fe.isControlEffect());
        }

        @Test
        @DisplayName("Expires a while-enchanted effect once the creature is no longer enchanted")
        void expiresWhileEnchantedEffect() {
            Permanent bear = addCreature(player2Id, "Grizzly Bears");
            creatureControlService.applyControlEffect(gd, player1Id, bear,
                    new GainControlOfEnchantedTargetEffect(), EffectDuration.PERMANENT,
                    null, "Rootwater Matriarch");
            assertThat(gd.playerBattlefields.get(player1Id)).contains(bear);

            lenient().when(gameQueryService.isEnchanted(gd, bear)).thenReturn(false);
            creatureControlService.reconcileControl(gd);

            assertThat(gd.playerBattlefields.get(player2Id)).contains(bear);
            assertThat(gd.floatingEffects).noneMatch(fe -> fe.isControlEffect());
        }

        @Test
        @DisplayName("Creates the WHILE_ATTACHED effect for an attached control Aura and steals the creature")
        void createsMissingControlAuraEffect() {
            Permanent bear = addCreature(player2Id, "Grizzly Bears");
            Card auraCard = createCreatureCard("In Bolas's Clutches");
            auraCard.addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
            Permanent aura = new Permanent(auraCard);
            aura.setAttachedTo(bear.getId());
            gd.playerBattlefields.get(player1Id).add(aura);

            creatureControlService.reconcileControl(gd);

            assertThat(gd.playerBattlefields.get(player1Id)).contains(bear);
            assertThat(gd.floatingEffects).anyMatch(fe -> fe.isControlEffect()
                    && fe.duration() == EffectDuration.WHILE_ATTACHED
                    && aura.getId().equals(fe.sourcePermanentId()));
        }

        @Test
        @DisplayName("Cleans up records for permanents that left the battlefield")
        void cleansUpDepartedPermanents() {
            UUID goneId = UUID.randomUUID();
            gd.stolenCreatures.put(goneId, player2Id);
            gd.addFloatingEffect(new com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect(
                    UUID.randomUUID(), "Test", null, player1Id,
                    new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                    goneId, null, null, EffectDuration.PERMANENT, 0));

            creatureControlService.reconcileControl(gd);

            assertThat(gd.stolenCreatures).doesNotContainKey(goneId);
            assertThat(gd.floatingEffects).noneMatch(fe -> fe.isControlEffect());
        }

        @Test
        @DisplayName("Aura control follows the Aura's current controller")
        void auraControlFollowsAuraController() {
            Permanent bear = addCreature(player2Id, "Grizzly Bears");
            Card auraCard = createCreatureCard("In Bolas's Clutches");
            auraCard.addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
            Permanent aura = new Permanent(auraCard);
            aura.setAttachedTo(bear.getId());
            gd.playerBattlefields.get(player1Id).add(aura);
            creatureControlService.applyControlEffect(gd, player1Id, bear,
                    new ControlEnchantedCreatureEffect(), EffectDuration.WHILE_ATTACHED,
                    aura.getId(), auraCard.getName());
            assertThat(gd.playerBattlefields.get(player1Id)).contains(bear);

            // Someone steals the Aura itself: the enchanted creature follows.
            gd.playerBattlefields.get(player1Id).remove(aura);
            gd.playerBattlefields.get(player2Id).add(aura);
            creatureControlService.reconcileControl(gd);

            assertThat(gd.playerBattlefields.get(player2Id)).contains(bear);
        }
    }

    @Nested
    @DisplayName("derived views")
    class DerivedViews {

        @Test
        @DisplayName("isStolenUntilEndOfTurn is true for a plain temporary steal")
        void stolenUntilEndOfTurn() {
            Permanent bear = addCreature(player2Id, "Grizzly Bears");
            applySteal(player1Id, bear, EffectDuration.UNTIL_END_OF_TURN, null);

            assertThat(gd.isStolenUntilEndOfTurn(bear.getId())).isTrue();
        }

        @Test
        @DisplayName("isStolenUntilEndOfTurn is false for a temporary effect on your own creature")
        void notStolenWhenOwnCreature() {
            Permanent bear = addCreature(player1Id, "Grizzly Bears");
            applySteal(player1Id, bear, EffectDuration.UNTIL_END_OF_TURN, null);

            assertThat(gd.isStolenUntilEndOfTurn(bear.getId())).isFalse();
        }

        @Test
        @DisplayName("isStolenUntilEndOfTurn is false when a permanent steal would survive cleanup")
        void notStolenWhenPermanentEffectHoldsIt() {
            Permanent bear = addCreature(player2Id, "Grizzly Bears");
            applySteal(player1Id, bear, EffectDuration.PERMANENT, null);
            applySteal(player1Id, bear, EffectDuration.UNTIL_END_OF_TURN, null);

            assertThat(gd.isStolenUntilEndOfTurn(bear.getId())).isFalse();
        }
    }
}
