package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEachControlledCreatureTokenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfEachControlledCreatureTokenEffectHandler implements NormalEffectHandlerBean {

    private final TokenCopySupport tokenCopySupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfEachControlledCreatureTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokenCopyOfEachControlledCreatureTokenEffect e =
                (CreateTokenCopyOfEachControlledCreatureTokenEffect) effect;

        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null || battlefield.isEmpty()) {
            return;
        }

        // Snapshot matching tokens first so the copies we create aren't themselves copied.
        List<Card> sourceCards = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (!permanent.getCard().isToken()) {
                continue;
            }
            if (e.creaturesOnly() && !gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }
            sourceCards.add(permanent.getCard());
        }

        tokenCopySupport.createTokenCopies(gameData, entry, sourceCards, null,
                new com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect());
    }

    void createTokenCopy(GameData gameData, StackEntry entry, Card sourceCard) {
        tokenCopySupport.createTokenCopies(gameData, entry, List.of(sourceCard), null,
                new com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect());
    }
}
