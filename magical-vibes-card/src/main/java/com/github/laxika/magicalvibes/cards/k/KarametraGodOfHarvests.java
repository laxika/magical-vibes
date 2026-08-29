package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.DevotionToColorsAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BNG", collectorNumber = "148")
public class KarametraGodOfHarvests extends Card {

    public KarametraGodOfHarvests() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new DevotionToColorsAtLeast(
                        Set.of(ManaColor.GREEN, ManaColor.WHITE), 7)),
                new SetCardTypesEffect(Set.of(CardType.ENCHANTMENT), GrantScope.SELF)));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        List.of(new SearchLibraryEffect(
                                new CardAnyOfPredicate(List.of(
                                        new CardSubtypePredicate(CardSubtype.FOREST),
                                        new CardSubtypePredicate(CardSubtype.PLAINS))),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED))),
                "Search your library for a Forest or Plains card?"));
    }
}
