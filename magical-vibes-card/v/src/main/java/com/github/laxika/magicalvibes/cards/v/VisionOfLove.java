package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "158")
public class VisionOfLove extends Card {

    public VisionOfLove() {
        addSpellEffect(new MayEffect(
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Sacrifice an artifact. If you do, draw two cards",
                                SequenceEffect.of(
                                        new SacrificePermanentsEffect(1, new PermanentIsArtifactPredicate(), Subjects.you()),
                                        new ConditionalEffect(HappenedThisWay.sacrificed(), new DrawCardEffect(2)))),
                        new ChooseOneEffect.ChooseOneOption(
                                "Discard a card. If you do, draw two cards",
                                SequenceEffect.of(
                                        new DiscardEffect(1, Subjects.you()),
                                        new ConditionalEffect(HappenedThisWay.discarded(),
                                                new DrawCardEffect(2))))
                )),
                "You may sacrifice an artifact or discard a card."));
    }
}
