package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BecomeCopyOfTargetCreatureEffectHandlerTest {

    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private ValidTargetService validTargetService;
    @Mock private GameQueryService gameQueryService;
    @Mock private CloneService cloneService;
    private final CopySupport copySupport = new CopySupport();
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private BecomeCopyOfTargetCreatureEffectHandler becomeCopyOfTargetCreatureHandler;

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
        becomeCopyOfTargetCreatureHandler = new BecomeCopyOfTargetCreatureEffectHandler(gameQueryService);

    }

    // ===== Helper methods =====

        private Card createCard(String name) {
            Card card = new Card();
            card.setName(name);
            return card;
        }

        private Card createSpellCard(String name, List<CardEffect> effects) {
            Card card = createCard(name);
            card.setType(CardType.INSTANT);
            return card;
        }

        private StackEntry spellEntry(Card card, UUID controllerId, StackEntryType type,
                                      List<CardEffect> effects, int xValue, UUID targetId) {
            return new StackEntry(type, card, controllerId, card.getName(), effects, xValue,
                    targetId, null, null, null, null, null);
        }

        private StackEntry spellEntry(Card card, UUID controllerId, StackEntryType type,
                                      List<CardEffect> effects, UUID targetId) {
            return spellEntry(card, controllerId, type, effects, 0, targetId);
        }

        private StackEntry copySpellTriggerEntry(Card twincastCard, UUID controllerId, UUID targetCardId) {
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, twincastCard, controllerId,
                    twincastCard.getName(), List.of(new CopySpellEffect()), 0,
                    targetCardId, null, null, null, null, null);
            return entry;
        }

        private Permanent createCreaturePermanent(String name, List<CardSubtype> subtypes) {
            Card card = createCard(name);
            card.setType(CardType.CREATURE);
            card.setSubtypes(subtypes);
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            return perm;
        }

        // =========================================================================
        // resolveCopySpell â€” CopySpellEffect
        // =========================================================================

    @Test
            @DisplayName("Queues a PendingMayAbility for the become-copy choice")
            void queuesMayAbilityForBecomeCopyChoice() {
                Card cloneCard = createCard("Clone");
                Permanent targetCreature = createCreaturePermanent("Serra Angel", List.of(CardSubtype.ANGEL));
                gd.playerBattlefields.get(player2Id).add(targetCreature);

                StackEntry entry = new StackEntry(StackEntryType.CREATURE_SPELL, cloneCard, player1Id,
                        cloneCard.getName(), List.of(new BecomeCopyOfTargetCreatureEffect()), 0,
                        targetCreature.getId(), null, null, null, null, null);

                when(gameQueryService.findPermanentById(gd, targetCreature.getId())).thenReturn(targetCreature);

                becomeCopyOfTargetCreatureHandler.resolve(gd, entry, new BecomeCopyOfTargetCreatureEffect());

                assertThat(gd.pendingMayAbilities).hasSize(1);
                PendingMayAbility ability = gd.pendingMayAbilities.getFirst();
                assertThat(ability.controllerId()).isEqualTo(player1Id);
                assertThat(ability.description()).contains("Clone");
                assertThat(ability.description()).contains("Serra Angel");
                assertThat(ability.targetCardId()).isEqualTo(targetCreature.getId());
            }

            @Test
            @DisplayName("Does nothing when targetId is null")
            void doesNothingWhenTargetIdIsNull() {
                Card cloneCard = createCard("Clone");
                StackEntry entry = new StackEntry(StackEntryType.CREATURE_SPELL, cloneCard, player1Id,
                        cloneCard.getName(), List.of(new BecomeCopyOfTargetCreatureEffect()), 0,
                        (UUID) null, null, null, null, null, null);

                becomeCopyOfTargetCreatureHandler.resolve(gd, entry, new BecomeCopyOfTargetCreatureEffect());

                assertThat(gd.pendingMayAbilities).isEmpty();
            }

            @Test
            @DisplayName("Does nothing when target creature is no longer on the battlefield")
            void doesNothingWhenTargetCreatureGone() {
                Card cloneCard = createCard("Clone");
                UUID removedCreatureId = UUID.randomUUID();
                StackEntry entry = new StackEntry(StackEntryType.CREATURE_SPELL, cloneCard, player1Id,
                        cloneCard.getName(), List.of(new BecomeCopyOfTargetCreatureEffect()), 0,
                        removedCreatureId, null, null, null, null, null);

                when(gameQueryService.findPermanentById(gd, removedCreatureId)).thenReturn(null);

                becomeCopyOfTargetCreatureHandler.resolve(gd, entry, new BecomeCopyOfTargetCreatureEffect());

                assertThat(gd.pendingMayAbilities).isEmpty();
            }

            @Test
            @DisplayName("PendingMayAbility effects contain BecomeCopyOfTargetCreatureEffect")
            void mayAbilityEffectsContainBecomeCopyEffect() {
                Card cloneCard = createCard("Clone");
                Permanent targetCreature = createCreaturePermanent("Grizzly Bears", List.of(CardSubtype.BEAR));
                gd.playerBattlefields.get(player2Id).add(targetCreature);

                StackEntry entry = new StackEntry(StackEntryType.CREATURE_SPELL, cloneCard, player1Id,
                        cloneCard.getName(), List.of(new BecomeCopyOfTargetCreatureEffect()), 0,
                        targetCreature.getId(), null, null, null, null, null);

                when(gameQueryService.findPermanentById(gd, targetCreature.getId())).thenReturn(targetCreature);

                becomeCopyOfTargetCreatureHandler.resolve(gd, entry, new BecomeCopyOfTargetCreatureEffect());

                PendingMayAbility ability = gd.pendingMayAbilities.getFirst();
                assertThat(ability.effects())
                        .hasSize(1)
                        .first()
                        .isInstanceOf(BecomeCopyOfTargetCreatureEffect.class);
            }
}
