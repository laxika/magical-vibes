package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SetAllOwnCreaturesBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "ELD", collectorNumber = "312")
public class OkosHospitality extends Card {

    public OkosHospitality() {
        addEffect(EffectSlot.SPELL, new SetAllOwnCreaturesBasePowerToughnessEffect(3, 3));
        addEffect(EffectSlot.SPELL, new MayEffect(
                new SearchLibraryAndOrGraveyardForCardToHandEffect(
                        new CardNamedPredicate("Oko, the Trickster")),
                "Search your library and/or graveyard for a card named Oko, the Trickster?"));
    }
}
