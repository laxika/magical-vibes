package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "179")
public class DawnhartMentor extends Card {

    public DawnhartMentor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect("Human", 1, 1, CardColor.WHITE, List.of(CardSubtype.HUMAN), Set.of(), Set.of()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{G}",
                List.of(new BoostTargetCreatureEffect(3, 3),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)),
                "Coven — {5}{G}: Target creature you control gets +3/+3 and gains trample until end of turn. "
                        + "Activate only if you control three or more creatures with different powers.",
                TargetFilters.creatureYouControl(),
                null,
                null,
                ActivationTimingRestriction.COVEN
        ));
    }
}
