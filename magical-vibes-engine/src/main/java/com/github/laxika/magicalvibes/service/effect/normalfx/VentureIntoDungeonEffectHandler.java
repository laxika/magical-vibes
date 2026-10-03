package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.AdditionalDungeonRoomTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CompleteDungeonEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VentureIntoDungeonEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TriggerCollectionService triggerCollectionService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final com.github.laxika.magicalvibes.service.trigger.TriggerTargetCollector triggerTargetCollector;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return VentureIntoDungeonEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        VentureIntoDungeonEffect venture = (VentureIntoDungeonEffect) effect;
        var playerId = entry.getControllerId();
        if (gameData.playersWhoVenturedIntoDungeonThisTurn.contains(playerId)
                && gameQueryService.playerHasDungeonVentureRestriction(gameData, playerId)) {
            return;
        }
        gameData.playersWhoVenturedIntoDungeonThisTurn.add(playerId);
        DungeonProgress progress = gameData.playerDungeonProgress.get(playerId);
        if (progress == null) {
            startDungeon(gameData, entry, playerId, venture.dungeon());
        } else if (progress.isBottomRoom()) {
            gameData.recordCompletedDungeon(playerId, progress.dungeon());
            gameData.playerDungeonProgress.remove(playerId);
            triggerCollectionService.checkDungeonCompletionTriggers(gameData, playerId);
            startDungeon(gameData, entry, playerId, venture.dungeon());
        } else {
            List<Integer> rooms = progress.dungeon().nextRooms(progress.roomIndex());
            if (rooms.size() > 1) {
                java.util.Map<String, Integer> choices = new java.util.LinkedHashMap<>();
                for (int room : rooms) choices.put(roomName(progress.dungeon(), room), room);
                interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.ColorChoice(
                        playerId, null, null,
                        new com.github.laxika.magicalvibes.model.ChoiceContext.VentureChoice(entry, progress.dungeon(), choices),
                        List.copyOf(choices.keySet()), "Choose the next dungeon room."));
            } else {
                DungeonProgress advanced = progress.advance();
                gameData.playerDungeonProgress.put(playerId, advanced);
                queueRoomAbility(gameData, entry, advanced);
            }
        }
    }

    private void startDungeon(GameData gameData, StackEntry entry, java.util.UUID playerId, Dungeon dungeon) {
        if (dungeon == null) {
            interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.ColorChoice(
                    playerId, null, null,
                    new com.github.laxika.magicalvibes.model.ChoiceContext.VentureChoice(entry, null, java.util.Map.of()),
                    List.of("Lost Mine of Phandelver", "Tomb of Annihilation", "Dungeon of the Mad Mage"),
                    "Choose a dungeon."));
            return;
        }
        DungeonProgress progress = new DungeonProgress(dungeon, 0);
        gameData.playerDungeonProgress.put(playerId, progress);
        queueRoomAbility(gameData, entry, progress);
    }

    /** Continues venture after the controller has chosen a dungeon or one of its outgoing rooms. */
    public void completeChoice(GameData gameData, com.github.laxika.magicalvibes.model.ChoiceContext.VentureChoice choice,
                                String selected) {
        if (choice.dungeon() == null) {
            Dungeon dungeon = switch (selected) {
                case "Lost Mine of Phandelver" -> Dungeon.LOST_MINE_OF_PHANDELVER;
                case "Tomb of Annihilation" -> Dungeon.TOMB_OF_ANNIHILATION;
                case "Dungeon of the Mad Mage" -> Dungeon.DUNGEON_OF_THE_MAD_MAGE;
                default -> throw new IllegalArgumentException("Invalid dungeon");
            };
            gameData.interaction.clearAwaitingInput();
            startDungeon(gameData, choice.sourceEntry(), choice.sourceEntry().getControllerId(), dungeon);
        } else {
            Integer room = choice.rooms().get(selected);
            if (room == null) throw new IllegalArgumentException("Invalid dungeon room");
            gameData.interaction.clearAwaitingInput();
            DungeonProgress progress = new DungeonProgress(choice.dungeon(), room);
            gameData.playerDungeonProgress.put(choice.sourceEntry().getControllerId(), progress);
            queueRoomAbility(gameData, choice.sourceEntry(), progress);
        }
    }

    private String roomName(Dungeon dungeon, int room) {
        return switch (dungeon) {
            case LOST_MINE_OF_PHANDELVER -> List.of("Cave Entrance", "Goblin Lair", "Mine Tunnels",
                    "Storeroom", "Dark Pool", "Fungi Cavern", "Temple of Dumathoin").get(room);
            case TOMB_OF_ANNIHILATION -> List.of("Trapped Entry", "Veils of Fear", "Sandfall Cell",
                    "Oubliette", "Cradle of the Death God").get(room);
            case DUNGEON_OF_THE_MAD_MAGE -> List.of("Yawning Portal", "Dungeon Level", "Goblin Bazaar",
                    "Twisted Caverns", "Lost Level", "Runestone Caverns", "Muiral's Graveyard",
                    "Deep Mines", "Mad Wizard's Lair").get(room);
            case UNDERCITY -> List.of("Secret Entrance", "Forge", "Lost Well", "Trap!", "Arena",
                    "Stash", "Archives", "Catacombs", "Throne of the Dead Three").get(room);
        };
    }

    private void queueRoomAbility(GameData gameData, StackEntry entry, DungeonProgress progress) {
        int previousCopies = gameData.beginTriggeredAbilityCopies(
                1 + countAdditionalDungeonRoomTriggers(gameData, entry.getControllerId()));
        try {
            Card sourceCard = new Card();
            sourceCard.setName(switch (progress.dungeon()) {
                case LOST_MINE_OF_PHANDELVER -> "Lost Mine of Phandelver";
                case TOMB_OF_ANNIHILATION -> "Tomb of Annihilation";
                case DUNGEON_OF_THE_MAD_MAGE -> "Dungeon of the Mad Mage";
                case UNDERCITY -> "Undercity";
            });
            sourceCard.setOwnerId(entry.getControllerId());
            List<CardEffect> effects = roomEffects(progress);
            if (effects.stream().anyMatch(roomEffect -> roomEffect.targetSpec() != com.github.laxika.magicalvibes.model.effect.TargetSpec.NONE)) {
                var targets = triggerTargetCollector.collect(gameData, effects, null, entry.getControllerId(),
                        sourceCard, com.github.laxika.magicalvibes.service.trigger.TriggerTargetCollector.Options.UPKEEP);
                if (!targets.validTargets().isEmpty()) {
                    gameData.queueInteraction(new com.github.laxika.magicalvibes.model.PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            sourceCard, entry.getControllerId(), effects, !targets.canTargetPermanents(),
                            null, 0, null, null, false, null, null, entry.getControllerId(), null, null, false));
                }
                return;
            }
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    entry.getControllerId(),
                    roomName(progress.dungeon(), progress.roomIndex()) + " room ability",
                    effects,
                    0,
                    (java.util.UUID) null));
        } finally {
            gameData.restoreTriggeredAbilityCopies(previousCopies);
        }
    }

    private int countAdditionalDungeonRoomTriggers(GameData gameData, java.util.UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return 0;
        }
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.isLosesAllAbilitiesUntilEndOfTurn()) {
                continue;
            }
            count += (int) permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .filter(AdditionalDungeonRoomTriggerEffect.class::isInstance)
                    .count();
        }
        return count;
    }

    private List<CardEffect> roomEffects(DungeonProgress progress) {
        if (progress.dungeon() == Dungeon.LOST_MINE_OF_PHANDELVER) {
            return switch (progress.roomIndex()) {
                case 0 -> List.of(new ScryEffect(1));
                case 1 -> List.of(creatureToken("Goblin", 1, 1, 1,
                        com.github.laxika.magicalvibes.model.CardColor.RED, List.of(CardSubtype.GOBLIN), java.util.Set.of(), false));
                case 2 -> List.of(CreateTokenEffect.ofTreasureToken(1));
                case 3 -> List.of(new PutCounterOnTargetPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()));
                case 4 -> List.of(
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(1));
                case 5 -> List.of(new com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect(-4, 0,
                        com.github.laxika.magicalvibes.model.effect.GrantDuration.UNTIL_YOUR_NEXT_TURN));
                case 6 -> List.of(new DrawCardEffect(1), new CompleteDungeonEffect(progress.dungeon()));
                default -> throw new IllegalStateException("Room index is outside Lost Mine of Phandelver");
            };
        }
        if (progress.dungeon() == Dungeon.TOMB_OF_ANNIHILATION) {
            var eligibleSacrifice = new com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate(List.of(
                    new com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate(),
                    new PermanentIsCreaturePredicate(),
                    new com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate()));
            return switch (progress.roomIndex()) {
                case 0 -> List.of(new LoseLifeEffect(1, LoseLifeRecipient.EACH_PLAYER));
                case 1 -> List.of(new com.github.laxika.magicalvibes.model.effect.EachPlayerMayDiscardOrLoseLifeEffect(2));
                case 2 -> List.of(new com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesPermanentOrLosesLifeEffect(
                        eligibleSacrifice, 2, "an artifact, creature, or land"));
                case 3 -> List.of(new com.github.laxika.magicalvibes.model.effect.DiscardEffect(1,
                        com.github.laxika.magicalvibes.model.effect.DiscardRecipient.CONTROLLER),
                        new com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect(1,
                                new com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate(),
                                com.github.laxika.magicalvibes.model.effect.SacrificeRecipient.CONTROLLER),
                        new com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect(1,
                                new PermanentIsCreaturePredicate(), com.github.laxika.magicalvibes.model.effect.SacrificeRecipient.CONTROLLER),
                        new com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect(1,
                                new com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate(),
                                com.github.laxika.magicalvibes.model.effect.SacrificeRecipient.CONTROLLER));
                case 4 -> List.of(creatureToken("The Atropal", 1, 4, 4,
                        com.github.laxika.magicalvibes.model.CardColor.BLACK, List.of(CardSubtype.GOD, CardSubtype.HORROR),
                        java.util.Set.of(Keyword.DEATHTOUCH), true), new CompleteDungeonEffect(progress.dungeon()));
                default -> throw new IllegalStateException("Room index is outside Tomb of Annihilation");
            };
        }
        if (progress.dungeon() == Dungeon.DUNGEON_OF_THE_MAD_MAGE) {
            return switch (progress.roomIndex()) {
                case 0 -> List.of(new GainLifeEffect(1));
                case 1 -> List.of(new ScryEffect(1));
                case 2 -> List.of(CreateTokenEffect.ofTreasureToken(1));
                case 3 -> List.of(new com.github.laxika.magicalvibes.model.effect.CantAttackThisTurnEffect(
                        com.github.laxika.magicalvibes.model.effect.TapUntapScope.TARGET, null,
                        com.github.laxika.magicalvibes.model.effect.GrantDuration.UNTIL_YOUR_NEXT_TURN));
                case 4 -> List.of(new ScryEffect(2));
                case 5 -> List.of(com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndMayCastSpellsEffect
                        .controllerMayPlayWithNormalCost(2));
                case 6 -> List.of(creatureToken("Skeleton", 2, 1, 1,
                        com.github.laxika.magicalvibes.model.CardColor.BLACK, List.of(CardSubtype.SKELETON), java.util.Set.of(), false));
                case 7 -> List.of(new ScryEffect(3));
                case 8 -> List.of(new DrawCardEffect(3),
                        new com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect(
                                null, null, null, true, true), new CompleteDungeonEffect(progress.dungeon()));
                default -> throw new IllegalStateException("Room index is outside Dungeon of the Mad Mage");
            };
        }
        if (progress.dungeon() == Dungeon.UNDERCITY) {
            return switch (progress.roomIndex()) {
                case 0 -> List.of(new SearchLibraryEffect(CardPredicateUtils.basicLand(),
                        LibrarySearchDestination.HAND));
                case 1 -> List.of(new PutCounterOnTargetPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 2, new PermanentIsCreaturePredicate()));
                case 2 -> List.of(new ScryEffect(2));
                case 3 -> List.of(new LoseLifeEffect(5, LoseLifeRecipient.TARGET_PLAYER));
                case 4 -> List.of(new GoadTargetCreatureUntilNextTurnEffect());
                case 5 -> List.of(CreateTokenEffect.ofTreasureToken(1));
                case 6 -> List.of(new DrawCardEffect(1));
                case 7 -> List.of(new CreateTokenEffect(
                        CardType.CREATURE, 1, "Skeleton", 4, 1, com.github.laxika.magicalvibes.model.CardColor.BLACK,
                        null, List.of(CardSubtype.SKELETON), java.util.Set.of(Keyword.MENACE), java.util.Set.of(), false, false,
                        java.util.Map.of(), java.util.List.of(), false, false, false, 0, java.util.Set.of()));
                case 8 -> List.of(new CompleteDungeonEffect(progress.dungeon()));
                default -> List.of();
            };
        }
        return List.of();
    }

    private CreateTokenEffect creatureToken(String name, int count, int power, int toughness,
                                             com.github.laxika.magicalvibes.model.CardColor color,
                                             List<CardSubtype> subtypes, java.util.Set<Keyword> keywords, boolean legendary) {
        return new CreateTokenEffect(CardType.CREATURE, count, name, power, toughness, color, null,
                subtypes, keywords, java.util.Set.of(), false, false, java.util.Map.of(), List.of(),
                false, false, legendary, 0, java.util.Set.of());
    }
}
