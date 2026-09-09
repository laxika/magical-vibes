package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "158")
public class VisionOfLove extends Card {

    public VisionOfLove() {
        addEffect(EffectSlot.SPELL, new MayEffect(
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Sacrifice an artifact. If you do, draw two cards",
                                new SacrificePermanentThenEffect(
                                        new PermanentIsArtifactPredicate(),
                                        new DrawCardEffect(2),
                                        "an artifact")),
                        new ChooseOneEffect.ChooseOneOption(
                                "Discard a card. If you do, draw two cards",
                                new DiscardCardThenEffect(
                                        null,
                                        new DrawCardEffect(2),
                                        "a card"))
                )),
                "You may sacrifice an artifact or discard a card."));
    }
}
