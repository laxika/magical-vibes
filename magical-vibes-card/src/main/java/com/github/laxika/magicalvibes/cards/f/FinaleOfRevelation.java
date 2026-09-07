package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeDuration;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "WAR", collectorNumber = "51")
public class FinaleOfRevelation extends Card {

    public FinaleOfRevelation() {
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new SpellXAtLeast(10),
                new DrawCardEffect(new XValue()),
                SequenceEffect.of(
                        new ShuffleGraveyardIntoLibraryEffect(false),
                        new DrawCardEffect(new XValue()),
                        new UntapPermanentsEffect(
                                TapUntapScope.ALL_PERMANENTS,
                                new PermanentIsLandPredicate(),
                                5),
                        new GrantNoMaximumHandSizeEffect(NoMaximumHandSizeDuration.REST_OF_GAME)
                )
        ));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
