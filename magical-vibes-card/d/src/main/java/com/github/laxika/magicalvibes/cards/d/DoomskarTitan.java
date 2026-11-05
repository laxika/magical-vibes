package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "130")
public class DoomskarTitan extends Card {

    public DoomskarTitan() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new BoostAllOwnCreaturesEffect(1, 0),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES));
        addCastingOption(new ForetellCast("{4}{R}"));
    }
}
