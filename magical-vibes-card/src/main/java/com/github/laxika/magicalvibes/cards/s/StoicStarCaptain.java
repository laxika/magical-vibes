package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PowerBoostForCrewAndStationEffect;
import com.github.laxika.magicalvibes.model.effect.SeekEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "3")
public class StoicStarCaptain extends Card {

    public StoicStarCaptain() {
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new PowerBoostForCrewAndStationEffect(2), GrantScope.ALL_OWN_CREATURES));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(new SeekEffect(new CardSubtypePredicate(CardSubtype.SPACECRAFT))),
                "Exhaust — {1}{W}: Seek a Spacecraft card. (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());
    }
}
