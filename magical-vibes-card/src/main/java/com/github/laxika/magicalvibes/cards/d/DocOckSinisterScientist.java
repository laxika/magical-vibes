package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "SPM", collectorNumber = "29")
public class DocOckSinisterScientist extends Card {

    public DocOckSinisterScientist() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new GraveyardCardThreshold(8, null),
                new SetBasePowerToughnessEffect(8, 8, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsAnotherPermanent(new PermanentHasSubtypePredicate(CardSubtype.VILLAIN)),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)));
    }
}
