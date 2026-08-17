package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MIR", collectorNumber = "32")
@CardRegistration(set = "6ED", collectorNumber = "33")
@CardRegistration(set = "7ED", collectorNumber = "29")
@CardRegistration(set = "8ED", collectorNumber = "33")
@CardRegistration(set = "9ED", collectorNumber = "31")
@CardRegistration(set = "10E", collectorNumber = "31")
@CardRegistration(set = "M10", collectorNumber = "22")
@CardRegistration(set = "M11", collectorNumber = "23")
@CardRegistration(set = "M12", collectorNumber = "28")
@CardRegistration(set = "M13", collectorNumber = "24")
@CardRegistration(set = "M14", collectorNumber = "25")
@CardRegistration(set = "M20", collectorNumber = "32")
@CardRegistration(set = "TPR", collectorNumber = "22")
@CardRegistration(set = "TMP", collectorNumber = "34")
@CardRegistration(set = "USG", collectorNumber = "27")
@CardRegistration(set = "ATH", collectorNumber = "10")
@CardRegistration(set = "BRB", collectorNumber = "48")
public class Pacifism extends Card {

    public Pacifism() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect());
    }
}
