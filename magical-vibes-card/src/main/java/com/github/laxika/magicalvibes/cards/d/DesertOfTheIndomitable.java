package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "172")
public class DesertOfTheIndomitable extends Card {

    public DesertOfTheIndomitable() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {G}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN)),
                "{T}: Add {G}."
        ));

        // Cycling {1}{G} ({1}{G}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{G}",
                List.of(new DrawCardEffect(1)),
                "Cycling {1}{G} ({1}{G}, Discard this card: Draw a card.)"));
    }
}
