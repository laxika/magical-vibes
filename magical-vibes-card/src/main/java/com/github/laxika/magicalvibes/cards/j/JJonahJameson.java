package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SuspectTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SPM", collectorNumber = "81")
@CardRegistration(set = "SPM", collectorNumber = "261")
public class JJonahJameson extends Card {

    public JJonahJameson() {
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SuspectTargetCreatureEffect());
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasKeywordPredicate(Keyword.MENACE),
                        CreateTokenEffect.ofTreasureToken(1)));
    }
}
