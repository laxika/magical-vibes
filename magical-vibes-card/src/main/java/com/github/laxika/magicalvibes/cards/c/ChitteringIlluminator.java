package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.AllowCastSourceCardFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;

import java.util.Set;

@CardRegistration(set = "YDSK", collectorNumber = "15")
public class ChitteringIlluminator extends Card {

    public ChitteringIlluminator() {
        addEffect(EffectSlot.STATIC, new AllowCastSourceCardFromTopOfLibraryEffect());
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new TopCardOfLibraryType(CardType.CREATURE),
                new LookAtTopCardOfOwnLibraryEffect()));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new TopCardOfLibraryType(CardType.CREATURE),
                new AllowCastFromTopOfLibraryEffect(Set.of(CardType.CREATURE))));
    }
}
