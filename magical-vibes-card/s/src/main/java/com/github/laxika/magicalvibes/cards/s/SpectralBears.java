package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "98")
@CardRegistration(set = "ATH", collectorNumber = "63")
@CardRegistration(set = "ME1", collectorNumber = "131")
public class SpectralBears extends Card {

    public SpectralBears() {
        // Whenever Spectral Bears attacks, if defending player controls no black nontoken
        // permanents, it doesn't untap during your next untap step.
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new NotCondition(new DefendingPlayerControlsPermanent(new PermanentAllOfPredicate(List.of(
                                        new PermanentColorInPredicate(Set.of(CardColor.BLACK)),
                                        new PermanentNotPredicate(new PermanentIsTokenPredicate())
                                ))))),
                new SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true));
    }
}
