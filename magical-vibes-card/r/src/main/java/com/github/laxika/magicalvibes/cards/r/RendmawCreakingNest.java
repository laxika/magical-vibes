package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "5")
public class RendmawCreakingNest extends Card {

    public RendmawCreakingNest() {
        EachPlayerCreatesTokenEffect birdTokens = new EachPlayerCreatesTokenEffect(birdToken());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, birdTokens);
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                SpellCastTriggerEffect.withAtLeastTwoCardTypes(List.of(birdTokens)));
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND,
                new TriggeringCardConditionalEffect(twoOrMoreCardTypesLand(), birdTokens));
    }

    private static CreateTokenEffect birdToken() {
        return new CreateTokenEffect(
                1,
                "Bird",
                2,
                2,
                CardColor.BLACK,
                List.of(CardSubtype.BIRD),
                Set.of(Keyword.FLYING),
                Set.of(),
                Map.of(EffectSlot.STATIC,
                        new GoadCreaturesUntilNextTurnEffect(new PermanentIsSourcePermanentPredicate())))
                .withTapped(true);
    }

    private static CardPredicate twoOrMoreCardTypesLand() {
        return new CardAnyOfPredicate(java.util.Arrays.stream(CardType.values())
                .filter(cardType -> cardType != CardType.LAND)
                .map(cardType -> new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.LAND),
                        new CardTypePredicate(cardType))))
                .map(predicate -> (CardPredicate) predicate)
                .toList());
    }
}
