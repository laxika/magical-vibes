package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseAllCreatureTypesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

@CardRegistration(set = "MIC", collectorNumber = "6")
@CardRegistration(set = "MIC", collectorNumber = "44")
public class CurseOfConformity extends Card {

    public CurseOfConformity() {
        PermanentPredicate nonlegendary = new PermanentNotPredicate(
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY));
        addEffect(EffectSlot.STATIC,
                new LoseAllCreatureTypesEffect(GrantScope.ENCHANTED_PLAYER_CREATURES, nonlegendary));
        addEffect(EffectSlot.STATIC,
                new SetBasePowerToughnessEffect(3, 3, GrantScope.ENCHANTED_PLAYER_CREATURES, nonlegendary));
    }
}
