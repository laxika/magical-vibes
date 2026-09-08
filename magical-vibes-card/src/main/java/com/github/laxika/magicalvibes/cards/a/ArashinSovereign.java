package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "212")
public class ArashinSovereign extends Card {

    public ArashinSovereign() {
        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("Top",
                                new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(0)),
                        new ChooseOneEffect.ChooseOneOption("Bottom",
                                new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(Integer.MAX_VALUE))
                )),
                "Put Arashin Sovereign on top or bottom of its owner's library?"));
    }
}
