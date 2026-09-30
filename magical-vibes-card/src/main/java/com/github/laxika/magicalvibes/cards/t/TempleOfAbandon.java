package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "THS", collectorNumber = "224")
@CardRegistration(set = "THB", collectorNumber = "244")
@CardRegistration(set = "WHO", collectorNumber = "313")
@CardRegistration(set = "WHO", collectorNumber = "523")
@CardRegistration(set = "WHO", collectorNumber = "904")
@CardRegistration(set = "WHO", collectorNumber = "1114")
@CardRegistration(set = "PIP", collectorNumber = "302")
@CardRegistration(set = "PIP", collectorNumber = "516")
@CardRegistration(set = "PIP", collectorNumber = "830")
@CardRegistration(set = "PIP", collectorNumber = "1044")
@CardRegistration(set = "40K", collectorNumber = "297")
@CardRegistration(set = "MKC", collectorNumber = "301")
public class TempleOfAbandon extends Card {

    public TempleOfAbandon() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(1));

        // {T}: Add {R}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));

        // {T}: Add {G}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
