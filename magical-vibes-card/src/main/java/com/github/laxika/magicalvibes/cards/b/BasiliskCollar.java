package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "WWK", collectorNumber = "122")
@CardRegistration(set = "MM3", collectorNumber = "216")
@CardRegistration(set = "SLD", collectorNumber = "1920")
@CardRegistration(set = "SLD", collectorNumber = "1925")
@CardRegistration(set = "2XM", collectorNumber = "233")
@CardRegistration(set = "PIP", collectorNumber = "225")
@CardRegistration(set = "PIP", collectorNumber = "480")
@CardRegistration(set = "PIP", collectorNumber = "753")
@CardRegistration(set = "PIP", collectorNumber = "1008")
@CardRegistration(set = "HBG", collectorNumber = "253")
@CardRegistration(set = "DSC", collectorNumber = "241")
public class BasiliskCollar extends Card {

    public BasiliskCollar() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
