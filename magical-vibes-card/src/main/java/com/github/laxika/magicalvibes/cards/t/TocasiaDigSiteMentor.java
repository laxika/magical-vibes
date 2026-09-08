package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "224")
public class TocasiaDigSiteMentor extends Card {

    public TocasiaDigSiteMentor() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ALL_OWN_CREATURES));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(true, null, List.of(new SurveilEffect(1)), "{T}: Surveil 1."),
                GrantScope.ALL_OWN_CREATURES));
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}{G}{W}{W}{U}{U}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        ReturnTargetCardsFromGraveyardToBattlefieldEffect.withinTotalManaValue(
                                new CardTypePredicate(CardType.ARTIFACT), 10)),
                "{2}{G}{G}{W}{W}{U}{U}, Exile this card from your graveyard: Return any number of target artifact cards with total mana value 10 or less from your graveyard to the battlefield. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
