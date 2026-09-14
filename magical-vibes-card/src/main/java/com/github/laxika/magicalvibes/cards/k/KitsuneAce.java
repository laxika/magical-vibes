package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "22")
public class KitsuneAce extends Card {

    public KitsuneAce() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.VEHICLE),
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "That Vehicle gains first strike until end of turn",
                                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TRIGGERING_PERMANENT)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Untap this creature",
                                        new UntapPermanentsEffect(TapUntapScope.SELF)
                        )))));
    }
}
