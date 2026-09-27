package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.CastFromLibraryTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordsToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ModifyCastSpellCharacteristicsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YMID", collectorNumber = "61")
public class WickerwingEffigy extends Card {

    public WickerwingEffigy() {
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        addEffect(EffectSlot.STATIC, new AllowCastFromTopOfLibraryEffect(Set.of(CardType.CREATURE)));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new CastFromLibraryTriggerEffect(List.of(
                new GrantKeywordsToCastSpellEffect(Set.of(Keyword.FLYING)),
                new ModifyCastSpellCharacteristicsEffect(CardColor.BLACK, CardSubtype.BIRD, 1, 1)
        )));
    }
}
