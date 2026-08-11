package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChosenSubtypeSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "253")
public class ChronicleOfVictory extends Card {

    public ChronicleOfVictory() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());
        addEffect(EffectSlot.STATIC, new BoostCreaturesOfChosenSubtypeEffect(2, 2));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Set.of(Keyword.FIRST_STRIKE, Keyword.TRAMPLE),
                GrantScope.OWN_CREATURES,
                new PermanentHasSourceChosenSubtypePredicate()));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new ChosenSubtypeSpellCastTriggerEffect(
                List.of(new DrawCardEffect()), false));
    }
}
