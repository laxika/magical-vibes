package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "165")
public class BrambleguardVeteran extends Card {

    public BrambleguardVeteran() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, SpellCastTriggerEffect.wheneverYouExpend(
                4,
                List.of(SequenceEffect.of(
                        new BoostAllOwnCreaturesEffect(1, 1,
                                new PermanentHasSubtypePredicate(CardSubtype.RACCOON)),
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ALL_OWN_CREATURES,
                                new PermanentHasSubtypePredicate(CardSubtype.RACCOON))
                ))));
    }
}
