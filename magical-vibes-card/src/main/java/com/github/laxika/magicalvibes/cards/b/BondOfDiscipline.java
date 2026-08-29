package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "WAR", collectorNumber = "6")
public class BondOfDiscipline extends Card {

    public BondOfDiscipline() {
        addEffect(EffectSlot.SPELL, new TapPermanentsEffect(
                TapUntapScope.ALL_CREATURES,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.OWN_CREATURES));
    }
}
