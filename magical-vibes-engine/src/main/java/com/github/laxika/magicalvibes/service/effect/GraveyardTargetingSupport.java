package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCategory;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GraveyardTargetingSupport {

    public Target findTarget(List<CardEffect> effects) {
        for (CardEffect effect : effects) {
            CardEffect targetEffect = unwrapMay(effect);
            Target target = targetOf(targetEffect);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    private CardEffect unwrapMay(CardEffect effect) {
        return effect instanceof MayEffect may ? may.wrapped() : effect;
    }

    private Target targetOf(CardEffect effect) {
        if (effect instanceof ExileGraveyardCardsEffect exile
                && effect.targetSpec().category().isGraveyard()) {
            return new Target(
                    exile.filter(),
                    effect.targetSpec().category() == TargetCategory.ANY_GRAVEYARD_CARD
                            ? GraveyardSearchScope.ALL_GRAVEYARDS
                            : GraveyardSearchScope.OPPONENT_GRAVEYARD,
                    "to exile");
        }
        if (effect instanceof ReturnCardFromGraveyardEffect returnEffect && returnEffect.targetGraveyard()) {
            String destination = switch (returnEffect.destination()) {
                case HAND -> "to your hand";
                case BATTLEFIELD -> "to the battlefield";
                case TOP_OF_OWNERS_LIBRARY -> "on top of its owner's library";
                case BOTTOM_OF_OWNERS_LIBRARY -> "on the bottom of its owner's library";
                case SHUFFLE_INTO_OWNERS_LIBRARY -> "into its owner's library";
                case EXILE -> "to exile";
                case MAY_ABILITY_TARGET -> "as chosen";
            };
            return new Target(returnEffect.filter(), returnEffect.source(), destination);
        }
        return null;
    }

    public record Target(CardPredicate filter, GraveyardSearchScope scope, String destination) {
    }
}
