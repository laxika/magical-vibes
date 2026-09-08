package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "216")
public class PyrewoodGearhulk extends Card {

    public PyrewoodGearhulk() {
        var otherCreatures = new PermanentNotPredicate(new PermanentIsSourceCardPredicate());

        // Other creatures you control get +2/+2 and gain vigilance and menace until end of turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostAllOwnCreaturesEffect(2, 2, otherCreatures));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GrantKeywordEffect(
                Set.of(Keyword.VIGILANCE, Keyword.MENACE), GrantScope.OWN_CREATURES, otherCreatures));

        // Damage can't be prevented this turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DamageCantBePreventedThisTurnEffect());
    }
}
