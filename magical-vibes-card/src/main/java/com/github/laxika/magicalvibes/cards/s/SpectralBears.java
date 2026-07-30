package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "98")
public class SpectralBears extends Card {

    public SpectralBears() {
        // Whenever Spectral Bears attacks, if defending player controls no black nontoken
        // permanents, it doesn't untap during your next untap step.
        addEffect(EffectSlot.ON_ATTACK,
                new ConditionalEffect(
                        new NotCondition(new DefendingPlayerControlsPermanent(new PermanentAllOfPredicate(List.of(
                                new PermanentColorInPredicate(Set.of(CardColor.BLACK)),
                                new PermanentNotPredicate(new PermanentIsTokenPredicate())
                        )))),
                        new SkipNextUntapEffect(TapUntapScope.SELF)));
    }
}
