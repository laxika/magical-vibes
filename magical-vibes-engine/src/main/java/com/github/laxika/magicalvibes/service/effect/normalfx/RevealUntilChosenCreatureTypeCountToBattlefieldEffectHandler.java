package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilChosenCreatureTypeCountToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Kindred Summons' chosen-type count reveal. */
@Component
@RequiredArgsConstructor
public class RevealUntilChosenCreatureTypeCountToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;
    private final RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffectHandler
            revealHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilChosenCreatureTypeCountToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (gameData.chosenSpellSubtype == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCreatureTypeChoice(gameData, controllerId);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        CardSubtype chosenSubtype = gameData.chosenSpellSubtype;
        gameData.chosenSpellSubtype = null;

        FilterContext filterContext = FilterContext.of(gameData).withSourceControllerId(controllerId);
        PermanentHasSubtypePredicate subtypePredicate = new PermanentHasSubtypePredicate(chosenSubtype);
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        int count = 0;
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && predicateEvaluationService.matchesPermanentPredicate(
                        permanent, subtypePredicate, filterContext)) {
                    count++;
                }
            }
        }

        CardAllOfPredicate matchingCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(chosenSubtype),
                        new CardKeywordPredicate(Keyword.CHANGELING)
                ))
        ));
        revealHandler.resolve(gameData, entry,
                new RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect(
                        new Fixed(count), matchingCreature, false, true), true);
    }
}
