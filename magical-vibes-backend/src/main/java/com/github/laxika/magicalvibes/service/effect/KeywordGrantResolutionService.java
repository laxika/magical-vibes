package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.GrantChosenKeywordToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDamageToOpponentCreatureBounceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromCardTypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordGrantResolutionService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @HandlesEffect(GrantKeywordEffect.class)
    private void resolveGrantKeyword(GameData gameData, StackEntry entry, GrantKeywordEffect grant) {
        if (grant.scope() == GrantScope.OWN_CREATURES) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                    .withSourceControllerId(entry.getControllerId());
            int count = 0;
            for (Permanent permanent : battlefield) {
                if (!gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }
                if (grant.filter() != null
                        && !gameQueryService.matchesPermanentPredicate(permanent, grant.filter(), filterContext)) {
                    continue;
                }
                permanent.getGrantedKeywords().add(grant.keyword());
                count++;
            }

            String keywordName = grant.keyword().name().charAt(0) + grant.keyword().name().substring(1).toLowerCase().replace('_', ' ');
            String logEntry = entry.getCard().getName() + " gives " + keywordName + " to " + count + " creature(s) until end of turn.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} grants {} to {} own creature(s)", gameData.id, entry.getCard().getName(), grant.keyword(), count);
            return;
        }

        if (grant.scope() == GrantScope.ALL_CREATURES) {
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                    .withSourceControllerId(entry.getControllerId());
            final int[] count = {0};
            gameData.forEachPermanent((playerId, permanent) -> {
                if (!gameQueryService.isCreature(gameData, permanent)) {
                    return;
                }
                if (grant.filter() != null
                        && !gameQueryService.matchesPermanentPredicate(permanent, grant.filter(), filterContext)) {
                    return;
                }
                permanent.getGrantedKeywords().add(grant.keyword());
                count[0]++;
            });

            String keywordName = grant.keyword().name().charAt(0) + grant.keyword().name().substring(1).toLowerCase().replace('_', ' ');
            String logEntry = entry.getCard().getName() + " gives " + keywordName + " to " + count[0] + " creature(s) until end of turn.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} grants {} to {} creature(s)", gameData.id, entry.getCard().getName(), grant.keyword(), count[0]);
            return;
        }

        UUID targetId = switch (grant.scope()) {
            case SELF -> entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetPermanentId();
            case TARGET -> entry.getTargetPermanentId();
            default -> null;
        };
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        target.getGrantedKeywords().add(grant.keyword());
        String keywordName = grant.keyword().name().charAt(0) + grant.keyword().name().substring(1).toLowerCase().replace('_', ' ');
        String logEntry = target.getCard().getName() + " gains " + keywordName + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gains {} ({})", gameData.id, target.getCard().getName(), grant.keyword(), grant.scope());
    }

    @HandlesEffect(GrantChosenKeywordToTargetEffect.class)
    private void resolveGrantChosenKeyword(GameData gameData, StackEntry entry, GrantChosenKeywordToTargetEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        playerInputService.beginKeywordChoice(gameData, entry.getControllerId(), target.getId(), effect.options());
    }

    @HandlesEffect(GrantColorUntilEndOfTurnEffect.class)
    private void resolveGrantColorUntilEndOfTurn(GameData gameData, StackEntry entry, GrantColorUntilEndOfTurnEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.getTransientColors().clear();
        target.getTransientColors().add(effect.color());
        target.setColorOverridden(true);

        String colorName = effect.color().name().charAt(0) + effect.color().name().substring(1).toLowerCase();
        String logEntry = target.getCard().getName() + " becomes " + colorName + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes {} until end of turn", gameData.id, target.getCard().getName(), colorName);
    }

    @HandlesEffect(GrantProtectionChoiceUntilEndOfTurnEffect.class)
    private void resolveGrantProtectionChoice(GameData gameData, StackEntry entry, GrantProtectionChoiceUntilEndOfTurnEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        playerInputService.beginProtectionColorChoice(gameData, entry.getControllerId(), target.getId(), effect.includeArtifacts());
    }

    @HandlesEffect(GrantProtectionFromCardTypeUntilEndOfTurnEffect.class)
    private void resolveGrantProtectionFromCardTypeUntilEndOfTurn(GameData gameData, StackEntry entry, GrantProtectionFromCardTypeUntilEndOfTurnEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.getProtectionFromCardTypes().add(effect.cardType());

        String typeName = effect.cardType().getDisplayName().toLowerCase() + "s";
        String logEntry = target.getCard().getName() + " gains protection from " + typeName + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gains protection from {} until end of turn", gameData.id, target.getCard().getName(), typeName);
    }

    @HandlesEffect(RemoveKeywordEffect.class)
    private void resolveRemoveKeyword(GameData gameData, StackEntry entry, RemoveKeywordEffect remove) {
        UUID targetId = switch (remove.scope()) {
            case SELF -> entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetPermanentId();
            case TARGET -> entry.getTargetPermanentId();
            default -> null;
        };
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        target.getRemovedKeywords().add(remove.keyword());
        String keywordName = remove.keyword().name().charAt(0) + remove.keyword().name().substring(1).toLowerCase().replace('_', ' ');
        String logEntry = target.getCard().getName() + " loses " + keywordName + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} loses {} ({})", gameData.id, target.getCard().getName(), remove.keyword(), remove.scope());
    }

    @HandlesEffect(GrantFlashbackToGraveyardCardsEffect.class)
    private void resolveGrantFlashbackToGraveyard(GameData gameData, StackEntry entry, GrantFlashbackToGraveyardCardsEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) {
            return;
        }

        int count = 0;
        for (Card card : graveyard) {
            boolean matchesType = false;
            for (CardType type : effect.cardTypes()) {
                if (card.hasType(type)) {
                    matchesType = true;
                    break;
                }
            }
            if (!matchesType) {
                continue;
            }
            // Skip cards that already have a native flashback option
            if (card.getCastingOption(FlashbackCast.class).isPresent()) {
                continue;
            }
            gameData.cardsGrantedFlashbackUntilEndOfTurn.add(card.getId());
            count++;
        }

        String logEntry = entry.getCard().getName() + " grants flashback to " + count + " card(s) in graveyard until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} grants flashback to {} graveyard card(s)", gameData.id, entry.getCard().getName(), count);
    }

    @HandlesEffect(GrantDamageToOpponentCreatureBounceUntilEndOfTurnEffect.class)
    private void resolveGrantDamageToOpponentCreatureBounce(GameData gameData, StackEntry entry) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        int count = 0;
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    permanent.setHasDamageToOpponentCreatureBounce(true);
                    count++;
                }
            }
        }

        String logEntry = entry.getCard().getName() + " grants damage-to-opponent creature bounce to " + count + " creature(s) until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} grants damage bounce to {} creature(s)", gameData.id, entry.getCard().getName(), count);
    }
}
