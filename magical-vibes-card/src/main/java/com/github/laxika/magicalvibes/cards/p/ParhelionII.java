package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.MakeCreatedPermanentsAttackingEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "24")
public class ParhelionII extends Card {

    public ParhelionII() {
        addEffect(EffectSlot.ON_ATTACK, new CreateTokenEffect(
                CardType.CREATURE,
                2,
                "Angel",
                4,
                4,
                CardColor.WHITE,
                null,
                List.of(CardSubtype.ANGEL),
                Set.of(Keyword.FLYING, Keyword.VIGILANCE),
                Set.of(),
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

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(4), AnimatePermanentsEffect.crew()),
                "Crew 4"
        ));
    }
}
