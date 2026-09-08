package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfTopLibraryCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "M21", collectorNumber = "139")
public class ConspicuousSnoop extends Card {

    public ConspicuousSnoop() {
        addEffect(EffectSlot.STATIC, new PlayWithTopCardRevealedEffect());
        addEffect(EffectSlot.STATIC,
                new AllowCastFromTopOfLibraryEffect(new CardSubtypePredicate(CardSubtype.GOBLIN)));
        addEffect(EffectSlot.STATIC,
                new GainActivatedAbilitiesOfTopLibraryCardEffect(new CardSubtypePredicate(CardSubtype.GOBLIN)));
    }
}
