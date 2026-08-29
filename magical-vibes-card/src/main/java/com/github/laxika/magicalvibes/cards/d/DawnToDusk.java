package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "BNG", collectorNumber = "6")
public class DawnToDusk extends Card {

    public DawnToDusk() {
        CardEffect returnEnchantment = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.ENCHANTMENT))
                .targetGraveyard(true)
                .build();
        DestroyTargetPermanentEffect destroyEnchantment = new DestroyTargetPermanentEffect();
        TargetFilter graveyardEnchantment = new GraveyardCardPredicateTargetFilter(
                new CardTypePredicate(CardType.ENCHANTMENT), GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
        PermanentPredicateTargetFilter enchantmentTarget = new PermanentPredicateTargetFilter(
                new PermanentIsEnchantmentPredicate(),
                "Target must be an enchantment.");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target enchantment card from your graveyard to your hand",
                        returnEnchantment,
                        graveyardEnchantment),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target enchantment",
                        destroyEnchantment,
                        enchantmentTarget),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target enchantment card from your graveyard to your hand and destroy target enchantment",
                        List.<CardEffect>of(returnEnchantment, destroyEnchantment),
                        List.of(graveyardEnchantment, enchantmentTarget))
        )));
    }
}
