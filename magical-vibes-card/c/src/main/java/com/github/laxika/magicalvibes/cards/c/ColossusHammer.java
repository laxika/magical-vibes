package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "M20", collectorNumber = "223")
@CardRegistration(set = "SLD", collectorNumber = "736")
@CardRegistration(set = "SLD", collectorNumber = "1866")
@CardRegistration(set = "GN3", collectorNumber = "115")
@CardRegistration(set = "AFC", collectorNumber = "202")
@CardRegistration(set = "LCC", collectorNumber = "110")
@CardRegistration(set = "FIC", collectorNumber = "338")
public class ColossusHammer extends Card {

    public ColossusHammer() {
        addStaticEffect(new StaticBoostEffect(10, 10, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new RemoveKeywordEffect(Keyword.FLYING, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{8}"));
    }
}
