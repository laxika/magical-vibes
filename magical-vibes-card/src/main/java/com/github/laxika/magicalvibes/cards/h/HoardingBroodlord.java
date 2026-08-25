package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToExileFaceDownPileEffect;

@CardRegistration(set = "MOM", collectorNumber = "110")
public class HoardingBroodlord extends Card {

    public HoardingBroodlord() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryForCardsToExileFaceDownPileEffect(1));
        addEffect(EffectSlot.STATIC, new AllowCastFromCardsExiledWithSourceEffect(false));
        addEffect(EffectSlot.STATIC, GrantSpellCastingAbilityToSpellsEffect.fromZone(
                Keyword.CONVOKE, null, Zone.EXILE));
    }
}
