package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureCardInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BecomeCopyOfTargetCreatureCardInGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCopierService permanentCopierService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfTargetCreatureCardInGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        UUID targetCardId = entry.getTargetCardIds().isEmpty()
                ? entry.getTargetId()
                : entry.getTargetCardIds().getFirst();
        Card graveyardCard = targetCardId == null
                ? null
                : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (graveyardCard == null
                || !graveyardCard.hasType(CardType.CREATURE)
                || graveyardCard.getManaValue() != entry.getXValue()
                || !entry.getControllerId().equals(gameQueryService.findGraveyardOwnerById(gameData, targetCardId))) {
            return;
        }

        BecomeCopyOfTargetCreatureCardInGraveyardEffect copyEffect =
                (BecomeCopyOfTargetCreatureCardInGraveyardEffect) effect;
        // The retained name and ability belong to the card this ability is on now: for a Clone copying
        // this card, that is the copied card rather than the Clone's original card.
        String retainedName = source.getCard().getName();
        List<ActivatedAbility> retainedAbilities = source.getCard().getActivatedAbilities().stream()
                .filter(ability -> ability.getEffects().contains(copyEffect))
                .limit(1)
                .toList();
        permanentCopierService.applyCloneCopy(source, graveyardCard, null, null, Set.of());
        gameData.activatedAbilityUsesThisTurn.remove(source.getId());

        Card copy = source.getCard();
        if (copyEffect.retainSourceName()) {
            copy.setName(retainedName);
        }

        if (copyEffect.addLegendarySupertype()) {
            Set<CardSupertype> supertypes = EnumSet.noneOf(CardSupertype.class);
            supertypes.addAll(copy.getSupertypes());
            supertypes.add(CardSupertype.LEGENDARY);
            copy.setSupertypes(supertypes);
        }

        if (!copyEffect.additionalKeywords().isEmpty()) {
            Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
            keywords.addAll(copy.getKeywords());
            keywords.addAll(copyEffect.additionalKeywords());
            copy.setKeywords(keywords);
        }

        for (ActivatedAbility retainedAbility : retainedAbilities) {
            copy.addActivatedAbility(retainedAbility);
        }

        gameLogService.append(gameData,
                GameLog.textCardText(retainedName + " becomes a copy of ", graveyardCard, "."));
    }
}
