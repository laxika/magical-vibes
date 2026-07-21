package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "125")
public class BantSojourners extends Card {

    public BantSojourners() {
        // When this creature dies, you may create a 1/1 white Soldier creature token.
        addEffect(EffectSlot.ON_DEATH, new MayEffect(CreateTokenEffect.whiteSoldier(1),
                "Create a 1/1 white Soldier creature token?"));

        // Cycling {2}{W} ({2}{W}, Discard this card: Draw a card.) — discard cost is intrinsic.
        // "When you cycle this card, you may create a 1/1 white Soldier creature token." The reflexive
        // cycle trigger rides on the cycling ability (Sojourners pattern): the may-token choice resolves
        // first, then the cycling draw resumes.
        addHandActivatedAbility(new ActivatedAbility(false, "{2}{W}",
                List.of(new MayEffect(CreateTokenEffect.whiteSoldier(1),
                                "Create a 1/1 white Soldier creature token?"),
                        new DrawCardEffect(1)),
                "Cycling {2}{W} ({2}{W}, Discard this card: Draw a card.)"));
    }
}
