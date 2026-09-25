package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "RTR", collectorNumber = "226")
@CardRegistration(set = "GRN", collectorNumber = "233")
@CardRegistration(set = "MPS", collectorNumber = "8")
@CardRegistration(set = "SLD", collectorNumber = "202")
@CardRegistration(set = "BRR", collectorNumber = "10")
@CardRegistration(set = "FCA", collectorNumber = "61")
@CardRegistration(set = "RVR", collectorNumber = "253")
@CardRegistration(set = "TMC", collectorNumber = "58")
@CardRegistration(set = "SLZ", collectorNumber = "96")
@CardRegistration(set = "SLZ", collectorNumber = "217")
@CardRegistration(set = "SLZ", collectorNumber = "338")
@CardRegistration(set = "ECC", collectorNumber = "138")
@CardRegistration(set = "CMM", collectorNumber = "376")
@CardRegistration(set = "CMM", collectorNumber = "602")
@CardRegistration(set = "SLD", collectorNumber = "2314")
@CardRegistration(set = "SLD", collectorNumber = "2329")
public class ChromaticLantern extends Card {

    public ChromaticLantern() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapForAnyColor(), GrantScope.OWN_LANDS));
    }
}
