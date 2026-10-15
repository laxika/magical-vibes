package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "38")
public class WhiteSunsTwilight extends Card {

    public WhiteSunsTwilight() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new XValue()));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE,
                new XValue(),
                "Mite",
                1,
                1,
                null,
                null,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.MITE),
                Set.of(Keyword.TOXIC),
                Set.of(CardType.ARTIFACT),
                false,
                false,
                Map.of(
                        EffectSlot.STATIC, new CantBlockEffect(),
                        EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER)
                ),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of()
        ));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new SpellXAtLeast(5),
                DestroyAllPermanentsEffect.sparingPermanentsCreatedThisResolution(new PermanentIsCreaturePredicate())));
    }
}
