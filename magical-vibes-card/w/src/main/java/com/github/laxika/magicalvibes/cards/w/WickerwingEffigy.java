package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.CastFromLibraryTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordsToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ModifyCastSpellCharacteristicsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YMID", collectorNumber = "61")
public class WickerwingEffigy extends Card {

    public WickerwingEffigy() {
        addStaticEffect(new LookAtTopCardOfOwnLibraryEffect());
        addStaticEffect(new AllowCastFromTopOfLibraryEffect(Set.of(CardType.CREATURE)));
        addTrigger(Triggers.youCastSpell(), new CastFromLibraryTriggerEffect(List.of(
                new GrantKeywordsToCastSpellEffect(Set.of(Keyword.FLYING)),
                new ModifyCastSpellCharacteristicsEffect(CardColor.BLACK, CardSubtype.BIRD, 1, 1)
        )));
    }
}
