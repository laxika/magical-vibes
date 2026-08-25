package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "237")
public class PalanisHatcher extends Card {

    public PalanisHatcher() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.HASTE,
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR)));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                2,
                "Dinosaur Egg",
                0,
                1,
                CardColor.GREEN,
                List.of(CardSubtype.DINOSAUR, CardSubtype.EGG),
                Set.of(),
                Set.of()));

        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCount(1, new PermanentHasSubtypePredicate(CardSubtype.EGG)),
                SequenceEffect.of(
                        new SacrificePermanentsEffect(
                                1,
                                new PermanentHasSubtypePredicate(CardSubtype.EGG),
                                SacrificeRecipient.CONTROLLER),
                        new CreateTokenEffect(
                                "Dinosaur",
                                3,
                                3,
                                CardColor.GREEN,
                                List.of(CardSubtype.DINOSAUR),
                                Set.of(),
                                Set.of()))));
    }
}
