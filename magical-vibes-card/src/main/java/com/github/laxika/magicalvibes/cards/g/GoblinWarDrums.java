package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "7ED", collectorNumber = "194")
@CardRegistration(set = "5ED", collectorNumber = "237")
@CardRegistration(set = "FEM", collectorNumber = "58a")
@CardRegistration(set = "FEM", collectorNumber = "58b")
@CardRegistration(set = "FEM", collectorNumber = "58c")
@CardRegistration(set = "FEM", collectorNumber = "120")
public class GoblinWarDrums extends Card {

    public GoblinWarDrums() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.MENACE, GrantScope.ALL_OWN_CREATURES));
    }
}
