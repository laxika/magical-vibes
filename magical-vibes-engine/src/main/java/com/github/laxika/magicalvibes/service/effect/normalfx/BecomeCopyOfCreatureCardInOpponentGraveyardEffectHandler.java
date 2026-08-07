package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfCreatureCardInOpponentGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import java.util.EnumSet;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeCopyOfCreatureCardInOpponentGraveyardEffectHandler implements NormalEffectHandlerBean {

    private static final EffectSlot SLOT = EffectSlot.ON_CREATURE_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE;

    private final GameQueryService gameQueryService;
    private final PermanentCopierService permanentCopierService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfCreatureCardInOpponentGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (BecomeCopyOfCreatureCardInOpponentGraveyardEffect) effect;
        if (e.graveyardCard() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            log.info("Game {} - Become-copy-of-graveyard-creature source no longer on the battlefield", gameData.id);
            return;
        }

        Card printed = source.getOriginalCard();
        permanentCopierService.applyCloneCopy(source, e.graveyardCard(), null, null, Set.of());

        Card copy = source.getCard();

        // "except its name is Lazav, Dimir Mastermind, it's legendary in addition to its other types,
        // and it has hexproof and this ability"
        copy.setName(printed.getName());

        Set<CardSupertype> supertypes = EnumSet.noneOf(CardSupertype.class);
        supertypes.addAll(copy.getSupertypes());
        supertypes.add(CardSupertype.LEGENDARY);
        copy.setSupertypes(supertypes);

        Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
        keywords.addAll(copy.getKeywords());
        keywords.add(Keyword.HEXPROOF);
        copy.setKeywords(keywords);

        for (EffectRegistration reg : printed.getEffectRegistrations(SLOT)) {
            copy.addEffect(SLOT, reg.effect(), reg.triggerMode());
        }

        gameLogService.append(gameData,
                GameLog.textCardText(printed.getName() + " becomes a copy of ", e.graveyardCard(), "."));
        log.info("Game {} - {} becomes a copy of {}", gameData.id, printed.getName(), e.graveyardCard().getName());
    }
}
