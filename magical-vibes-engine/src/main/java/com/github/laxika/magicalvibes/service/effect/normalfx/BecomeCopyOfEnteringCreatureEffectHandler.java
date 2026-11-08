package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.service.trigger.ZoneChangeWatch;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeCopyOfEnteringCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCopierService permanentCopierService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfEnteringCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            log.info("Game {} - Become-copy-of-entering source no longer on the battlefield", gameData.id);
            return;
        }

        // "That creature" is the stack entry's triggering permanent: as it is now, else as it last existed on the
        // battlefield (CR 608.2h), its copiable values then (CR 707.2).
        UUID enteringId = entry.getTriggeringPermanentId();
        Permanent entering = enteringId == null ? null : gameQueryService.findPermanentById(gameData, enteringId);
        if (entering == null && enteringId != null) {
            entering = gameData.departedPermanentSnapshots.values().stream()
                    .filter(snapshot -> snapshot.getId().equals(enteringId))
                    .findFirst()
                    .orElse(null);
        }
        if (entering == null && entry.getAttachedPermanentSnapshot() != null
                && entry.getAttachedPermanentSnapshot().getId().equals(enteringId)) {
            entering = entry.getAttachedPermanentSnapshot();
        }
        if (entering == null) {
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                    "'s ability fizzles (the creature that entered is no longer on the battlefield)."));
            log.info("Game {} - Become-copy-of-entering fizzles, entering permanent gone", gameData.id);
            return;
        }

        String originalName = source.getCard().getName();
        List<TriggeredAbility> copyAbilities = ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD
                .abilitiesOf(source.getCard()).stream()
                .filter(ability -> ability.effects().stream()
                        .anyMatch(registration -> registration.effect() instanceof BecomeCopyOfEnteringCreatureEffect))
                .toList();
        Card enteringCard = entering.getCard();
        if (entering.isFaceDown()) {
            enteringCard = new Card();
            enteringCard.setName(null);
            enteringCard.setType(com.github.laxika.magicalvibes.model.CardType.CREATURE);
            enteringCard.setPower(entering.getFaceDownPower());
            enteringCard.setToughness(entering.getFaceDownToughness());
        }
        permanentCopierService.applyCloneCopy(source, enteringCard, null, null, Set.of());

        // "except it has this ability" — re-grant the copy ability this permanent had before copying,
        // which may come from a copy effect (e.g. Clone) rather than the printed card.
        Card copiedCard = source.getCard();
        copyAbilities.forEach(copiedCard::addTriggeredAbility);

        gameLogService.append(gameData, GameLog.textCardText(originalName + " becomes a copy of ", enteringCard, "."));
        log.info("Game {} - {} becomes a copy of {}", gameData.id, originalName, enteringCard.getName());
    }
}
