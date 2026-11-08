package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CopyEnchantedInstantAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellweaverVoluteEnterEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "59")
public class SpellweaverVolute extends Card {

    public SpellweaverVolute() {
        target(new GraveyardCardPredicateTargetFilter(
                new CardTypePredicate(CardType.INSTANT), GraveyardSearchScope.ALL_GRAVEYARDS));
        addSpellEffect(new SpellweaverVoluteEnterEffect());
        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.SORCERY)),
                new MayEffect(new CopyEnchantedInstantAndMayCastCopyEffect(),
                        "You may cast a copy of the enchanted instant without paying its mana cost?"));
    }
}
