package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "229")
public class NaomiPillarOfOrder extends Card {

    public NaomiPillarOfOrder() {
        ConditionalEffect createSamurai = new ConditionalEffect(
                new AllOf(List.of(
                        new ControlsPermanent(new PermanentIsArtifactPredicate()),
                        new ControlsPermanent(new PermanentIsEnchantmentPredicate()))),
                new CreateTokenEffect(
                        1,
                        "Samurai",
                        2,
                        2,
                        CardColor.WHITE,
                        List.of(CardSubtype.SAMURAI),
                        Set.of(Keyword.VIGILANCE),
                        Set.of()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, createSamurai);
        addEffect(EffectSlot.ON_ATTACK, createSamurai);
    }
}
