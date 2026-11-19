package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C16", collectorNumber = "18")
public class FrenziedFugue extends Card {

    public FrenziedFugue() {
        target(TargetFilters.permanent());

        SequenceEffect effect = SequenceEffect.of(
                new GainControlOfEnchantedPermanentEffect(Duration.UNTIL_END_OF_TURN),
                new UntapPermanentsEffect(Subjects.attached()),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.ENCHANTED_PERMANENT));

        addTrigger(Triggers.enters(ObjectRelation.THIS), effect);
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), effect);
    }
}
