package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MID", collectorNumber = "72")
@CardRegistration(set = "DBL", collectorNumber = "72")
public class RevengeOfTheDrowned extends Card {

    public RevengeOfTheDrowned() {
        target(TargetFilters.creature())
                .addSpellEffect(new PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect())
                .addSpellEffect(CreateTokenEffect.blackZombieWithDecayed(1));
    }
}
