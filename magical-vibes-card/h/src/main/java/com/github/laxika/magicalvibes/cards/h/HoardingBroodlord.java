package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExileAccessScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToExileFaceDownPileEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "110")
@CardRegistration(set = "MOM", collectorNumber = "360")
public class HoardingBroodlord extends Card {

    public HoardingBroodlord() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SearchLibraryForCardsToExileFaceDownPileEffect(1));
        addStaticEffect(new AllowCastFromCardsExiledWithSourceEffect(
                false, null, false, false, 0, null, false, false, false, ExileAccessScope.EXILER,
                false, false, true, false, false, null, false, false, false, false));
        addStaticEffect(GrantSpellCastingAbilityToSpellsEffect.fromZone(
                Keyword.CONVOKE, null, Zone.EXILE));
    }
}
