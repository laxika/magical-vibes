package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "52")
public class GitaxianAnatomist extends Card {

    public GitaxianAnatomist() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new ConditionalEffect(
                        new AllOf(List.of(new SourceIsOnBattlefield(), new SourceUntapped())),
                        SequenceEffect.of(
                                new TapPermanentsEffect(TapUntapScope.SELF),
                                new ProliferateEffect())),
                "Tap Gitaxian Anatomist and proliferate?"));
    }
}
