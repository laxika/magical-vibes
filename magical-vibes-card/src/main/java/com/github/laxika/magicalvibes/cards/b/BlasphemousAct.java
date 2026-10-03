package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ISD", collectorNumber = "130")
@CardRegistration(set = "SLD", collectorNumber = "322")
@CardRegistration(set = "SLD", collectorNumber = "1756")
@CardRegistration(set = "SLD", collectorNumber = "1998")
@CardRegistration(set = "2XM", collectorNumber = "117")
@CardRegistration(set = "SOC", collectorNumber = "238")
@CardRegistration(set = "MSC", collectorNumber = "163")
@CardRegistration(set = "MSC", collectorNumber = "358")
@CardRegistration(set = "ECC", collectorNumber = "89")
@CardRegistration(set = "TLE", collectorNumber = "26")
@CardRegistration(set = "TMC", collectorNumber = "47")
@CardRegistration(set = "C14", collectorNumber = "172")
@CardRegistration(set = "WHO", collectorNumber = "224")
@CardRegistration(set = "WHO", collectorNumber = "472")
@CardRegistration(set = "WHO", collectorNumber = "815")
@CardRegistration(set = "WHO", collectorNumber = "1063")
@CardRegistration(set = "PIP", collectorNumber = "188")
@CardRegistration(set = "PIP", collectorNumber = "465")
@CardRegistration(set = "PIP", collectorNumber = "716")
@CardRegistration(set = "PIP", collectorNumber = "993")
@CardRegistration(set = "C21", collectorNumber = "159")
@CardRegistration(set = "40K", collectorNumber = "204")
@CardRegistration(set = "NCC", collectorNumber = "264")
@CardRegistration(set = "DSC", collectorNumber = "160")
@CardRegistration(set = "LTC", collectorNumber = "211")
@CardRegistration(set = "TDC", collectorNumber = "207")
@CardRegistration(set = "LCC", collectorNumber = "216")
@CardRegistration(set = "BLC", collectorNumber = "114")
@CardRegistration(set = "DRC", collectorNumber = "101")
@CardRegistration(set = "BRC", collectorNumber = "113")
@CardRegistration(set = "C18", collectorNumber = "120")
@CardRegistration(set = "EOC", collectorNumber = "86")
public class BlasphemousAct extends Card {

    public BlasphemousAct() {
        // Blasphemous Act costs {1} less to cast for each creature on the battlefield.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.ANY_PLAYER)));
        addEffect(EffectSlot.SPELL, new MassDamageEffect(13));
    }
}
