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
@CardRegistration(set = "MB2", collectorNumber = "225")
@CardRegistration(set = "2XM", collectorNumber = "267")
@CardRegistration(set = "SOC", collectorNumber = "350")
@CardRegistration(set = "MSC", collectorNumber = "202")
@CardRegistration(set = "CMD", collectorNumber = "253")
@CardRegistration(set = "C15", collectorNumber = "257")
@CardRegistration(set = "CMM", collectorNumber = "398")
@CardRegistration(set = "WHO", collectorNumber = "243")
@CardRegistration(set = "WHO", collectorNumber = "834")
@CardRegistration(set = "PIP", collectorNumber = "233")
@CardRegistration(set = "PIP", collectorNumber = "761")
@CardRegistration(set = "FIC", collectorNumber = "349")
@CardRegistration(set = "DSC", collectorNumber = "93")
@CardRegistration(set = "LTC", collectorNumber = "281")
@CardRegistration(set = "TDC", collectorNumber = "102")
@CardRegistration(set = "M3C", collectorNumber = "298")
@CardRegistration(set = "OTC", collectorNumber = "260")
@CardRegistration(set = "LCC", collectorNumber = "114")
@CardRegistration(set = "CM2", collectorNumber = "196")
@CardRegistration(set = "C20", collectorNumber = "245")
@CardRegistration(set = "C19", collectorNumber = "217")
@CardRegistration(set = "C16", collectorNumber = "260")
@CardRegistration(set = "C17", collectorNumber = "215")
@CardRegistration(set = "DRC", collectorNumber = "55")
@CardRegistration(set = "SCD", collectorNumber = "269")
@CardRegistration(set = "CMA", collectorNumber = "220")
@CardRegistration(set = "ARC", collectorNumber = "110")
public class LightningGreaves extends Card {

    public LightningGreaves() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.SHROUD, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{0}"));
    }
}
