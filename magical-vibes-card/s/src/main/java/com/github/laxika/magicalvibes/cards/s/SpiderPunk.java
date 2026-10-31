package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RiotEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsAndAbilitiesCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "SPM", collectorNumber = "92")
@CardRegistration(set = "SPM", collectorNumber = "207")
@CardRegistration(set = "SPM", collectorNumber = "210")
@CardRegistration(set = "OM1", collectorNumber = "85")
public class SpiderPunk extends Card {

    public SpiderPunk() {
        addStaticEffect(new RiotEffect());
        addStaticEffect(new GrantKeywordEffect(
                Keyword.RIOT,
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SPIDER)));
        addStaticEffect(new SpellsAndAbilitiesCantBeCounteredEffect());
        addStaticEffect(new DamageCantBePreventedEffect());
    }
}
