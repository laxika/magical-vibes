package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardAndBoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffectHandler
        implements NormalEffectHandlerBean {

    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffect) effect;
        UUID controllerId = entry.getControllerId();
        boolean canSacrifice = !validSacrificeIds(gameData, entry, e).isEmpty();
        List<Card> hand = gameData.playerHands.get(controllerId);
        boolean canDiscard = hand != null && !hand.isEmpty();

        if (!canSacrifice && !canDiscard) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId)
                            + " has no " + e.sacrificeDescription() + " to sacrifice and no cards to discard."));
            return;
        }

        CardEffect sacrifice = sacrificeEffect(e);
        CardEffect discard = new DiscardCardAndBoostSelfEffect(e.power(), e.toughness(), e.drawCount());
        if (canSacrifice && canDiscard) {
            ChooseOneEffect choice = new ChooseOneEffect(List.of(
                    new ChooseOneEffect.ChooseOneOption(
                            "Sacrifice " + e.sacrificeDescription(), sacrifice),
                    new ChooseOneEffect.ChooseOneOption("Discard a card", discard)));
            playerInputService.beginChooseModeChoice(gameData, controllerId, entry.getCard(), choice,
                    false, entry.getSourcePermanentId());
            return;
        }

        dispatch(gameData, entry, canSacrifice ? sacrifice : discard);
    }

    private CardEffect sacrificeEffect(SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffect effect) {
        return new SacrificePermanentThenEffect(
                effect.sacrificeFilter(),
                SequenceEffect.of(
                        new DrawCardEffect(effect.drawCount()),
                        new BoostSelfEffect(effect.power(), effect.toughness())),
                effect.sacrificeDescription(), false, false);
    }

    private List<UUID> validSacrificeIds(GameData gameData, StackEntry entry,
                                         SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffect effect) {
        UUID controllerId = entry.getControllerId();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(controllerId)
                .withSourcePermanentId(entry.getSourcePermanentId());
        List<UUID> validIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(
                        permanent, effect.sacrificeFilter(), filterContext)) {
                    validIds.add(permanent.getId());
                }
            }
        }
        return validIds;
    }

    private void dispatch(GameData gameData, StackEntry entry, CardEffect effect) {
        EffectHandler handler = effectHandlerRegistry.getHandler(effect);
        if (handler == null) {
            throw new IllegalStateException("No handler for " + effect.getClass().getSimpleName());
        }
        handler.resolve(gameData, entry, effect);
    }
}
