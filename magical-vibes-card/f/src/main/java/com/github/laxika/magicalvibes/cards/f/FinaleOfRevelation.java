package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "WAR", collectorNumber = "51")
@CardRegistration(set = "M3C", collectorNumber = "185")
@CardRegistration(set = "MKC", collectorNumber = "106")
@CardRegistration(set = "OTC", collectorNumber = "97")
public class FinaleOfRevelation extends Card {

    public FinaleOfRevelation() {
        addSpellEffect(new ConditionalReplacementEffect(
                new SpellXAtLeast(10),
                new DrawCardEffect(new XValue()),
                SequenceEffect.of(
                        new ShuffleGraveyardIntoLibraryEffect(false),
                        new DrawCardEffect(new XValue()),
                        new UntapPermanentsEffect(
                                Subjects.controlledBy(Subjects.eachPlayer(), new PermanentIsLandPredicate()), null,
                                5),
                        new GrantNoMaximumHandSizeEffect(Duration.PERMANENT)
                )
        ));
        addSpellEffect(new ExileSpellEffect());
    }
}
