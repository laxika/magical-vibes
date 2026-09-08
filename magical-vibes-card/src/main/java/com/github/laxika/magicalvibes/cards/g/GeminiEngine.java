package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.amount.SourceToughness;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatedPermanentsAttackingEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndOfCombatEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DST", collectorNumber = "121")
public class GeminiEngine extends Card {

    public GeminiEngine() {
        addEffect(EffectSlot.ON_ATTACK, new CreateTokenEffect(
                CardType.CREATURE,
                new Fixed(1),
                "Twin",
                new SourcePower(),
                new SourceToughness(),
                null,
                null,
                List.of(CardSubtype.CONSTRUCT),
                Set.of(),
                Set.of(CardType.ARTIFACT),
                false,
                false,
                Map.of(),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of()));
        addEffect(EffectSlot.ON_ATTACK, new MakeCreatedPermanentsAttackingEffect());
        addEffect(EffectSlot.ON_ATTACK, new SacrificeCreatedPermanentsAtEndOfCombatEffect());
    }
}
