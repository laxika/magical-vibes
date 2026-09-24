package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "705")
@CardRegistration(set = "CMM", collectorNumber = "773")
@CardRegistration(set = "CMM", collectorNumber = "780")
public class AniktheaHandOfErebos extends Card {

    public AniktheaHandOfErebos() {
        CardPredicate nonAuraEnchantment = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.ENCHANTMENT),
                new CardNotPredicate(new CardIsAuraPredicate())));

        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.MENACE,
                GrantScope.OWN_CREATURES,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsEnchantmentPredicate()))));

        var graveyardTarget = target(new GraveyardCardPredicateTargetFilter(
                nonAuraEnchantment,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 1);
        ExileTargetCardFromGraveyardAndCreateTokenCopyEffect copyEffect =
                new ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
                        nonAuraEnchantment,
                        true,
                        List.of(CardSubtype.ZOMBIE),
                        false,
                        false,
                        CardColor.BLACK,
                        3,
                        3);
        graveyardTarget.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, copyEffect);
        graveyardTarget.addEffect(EffectSlot.ON_ATTACK, copyEffect);
    }
}
