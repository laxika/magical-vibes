package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.DevotionToColorAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "150")
public class PurphorosBronzeBlooded extends Card {

    public PurphorosBronzeBlooded() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new DevotionToColorAtLeast(ManaColor.RED, 5)),
                new SetCardTypesEffect(Set.of(CardType.ENCHANTMENT), GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(
                                new CardAnyOfPredicate(List.of(
                                        new CardAllOfPredicate(List.of(
                                                new CardColorPredicate(CardColor.RED),
                                                new CardTypePredicate(CardType.CREATURE))),
                                        new CardAllOfPredicate(List.of(
                                                new CardTypePredicate(CardType.ARTIFACT),
                                                new CardTypePredicate(CardType.CREATURE))))),
                                "red creature or artifact creature", false, false, false, true),
                        "Put a red creature card or an artifact creature card from your hand onto the battlefield?"
                )),
                "{2}{R}: You may put a red creature card or an artifact creature card from your hand onto the battlefield. "
                        + "Sacrifice it at the beginning of the next end step."
        ));
    }
}
