package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "MRD", collectorNumber = "199")
@CardRegistration(set = "DDE", collectorNumber = "19")
@CardRegistration(set = "MPS", collectorNumber = "14")
@CardRegistration(set = "SLD", collectorNumber = "99")
@CardRegistration(set = "SLD", collectorNumber = "1979")
@CardRegistration(set = "SLD", collectorNumber = "1987")
@CardRegistration(set = "SLD", collectorNumber = "1992")
@CardRegistration(set = "SLD", collectorNumber = "1493")
@CardRegistration(set = "SLD", collectorNumber = "1662")
@CardRegistration(set = "SLD", collectorNumber = "2062")
@CardRegistration(set = "SLD", collectorNumber = "2099")
@CardRegistration(set = "SLD", collectorNumber = "2465")
@CardRegistration(set = "MB1", collectorNumber = "217")
@CardRegistration(set = "2XM", collectorNumber = "267")
@CardRegistration(set = "SOC", collectorNumber = "350")
@CardRegistration(set = "MSC", collectorNumber = "202")
@CardRegistration(set = "CMD", collectorNumber = "253")
@CardRegistration(set = "C15", collectorNumber = "257")
@CardRegistration(set = "CMM", collectorNumber = "398")
public class LightningGreaves extends Card {

    public LightningGreaves() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.SHROUD, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{0}"));
    }
}
