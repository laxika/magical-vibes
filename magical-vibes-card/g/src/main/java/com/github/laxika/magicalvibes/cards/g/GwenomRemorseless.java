package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "56")
@CardRegistration(set = "SPM", collectorNumber = "256")
@CardRegistration(set = "SPM", collectorNumber = "286")
@CardRegistration(set = "OM1", collectorNumber = "56")
public class GwenomRemorseless extends Card {

    public GwenomRemorseless() {
        addTrigger(Triggers.attacks(),
                GrantStaticEffectToPlayerEffect.untilEndOfTurn(new LookAtTopCardOfOwnLibraryEffect()),
                GrantStaticEffectToPlayerEffect.untilEndOfTurn(new PlayLandsFromTopOfLibraryEffect()),
                GrantStaticEffectToPlayerEffect.untilEndOfTurn(
                        AllowCastFromTopOfLibraryEffect.anySpell().payingLifeEqualToManaValue()));
    }
}
