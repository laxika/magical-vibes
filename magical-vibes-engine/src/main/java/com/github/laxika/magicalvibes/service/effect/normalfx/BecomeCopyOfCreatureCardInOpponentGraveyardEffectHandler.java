package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfCreatureCardInOpponentGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeCopyOfCreatureCardInOpponentGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCopierService permanentCopierService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfCreatureCardInOpponentGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTriggeringCards().isEmpty()) {
            return;
        }
        Card graveyardCard = entry.getTriggeringCards().getFirst();

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            log.info("Game {} - Become-copy-of-graveyard-creature source no longer on the battlefield", gameData.id);
            return;
        }

        // The ability and name belong to the card this ability is on now, which for a Clone copying
        // this card is not its original card.
        String retainedName = source.getCard().getName();
        List<TriggeredAbility> retainedAbilities = source.getCard().getTriggeredAbilities().stream()
                .filter(ability -> ability.effects().stream().anyMatch(reg -> reg.effect() instanceof MayEffect may
                        && may.wrapped() instanceof BecomeCopyOfCreatureCardInOpponentGraveyardEffect))
                .toList();
        permanentCopierService.applyCloneCopy(source, graveyardCard, null, null, Set.of());

        Card copy = source.getCard();

        // "except its name is Lazav, Dimir Mastermind, it's legendary in addition to its other types,
        // and it has hexproof and this ability"
        copy.setName(retainedName);

        Set<CardSupertype> supertypes = EnumSet.noneOf(CardSupertype.class);
        supertypes.addAll(copy.getSupertypes());
        supertypes.add(CardSupertype.LEGENDARY);
        copy.setSupertypes(supertypes);

        Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
        keywords.addAll(copy.getKeywords());
        keywords.add(Keyword.HEXPROOF);
        copy.setKeywords(keywords);

        for (TriggeredAbility ability : retainedAbilities) {
            copy.addTriggeredAbility(ability);
        }

        gameLogService.append(gameData,
                GameLog.textCardText(retainedName + " becomes a copy of ", graveyardCard, "."));
        log.info("Game {} - {} becomes a copy of {}", gameData.id, retainedName, graveyardCard.getName());
    }
}
