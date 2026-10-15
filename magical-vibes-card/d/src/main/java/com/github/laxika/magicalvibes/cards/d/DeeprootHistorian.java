package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "59")
@CardRegistration(set = "LCC", collectorNumber = "91")
public class DeeprootHistorian extends Card {

    public DeeprootHistorian() {
        addEffect(EffectSlot.STATIC, GrantSpellCastingAbilityToSpellsEffect.fromZone(
                Keyword.RETRACE,
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.MERFOLK),
                        new CardSubtypePredicate(CardSubtype.DRUID))),
                Zone.GRAVEYARD));
    }
}
