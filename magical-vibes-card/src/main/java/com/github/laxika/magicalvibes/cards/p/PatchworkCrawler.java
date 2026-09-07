package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardPutCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfExiledCardsEffect;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "72")
public class PatchworkCrawler extends Card {

    public PatchworkCrawler() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new ExileTargetCreatureCardFromGraveyardPutCounterOnSourceEffect(
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD)),
                "{2}{U}: Exile target creature card from your graveyard and put a +1/+1 counter on this creature."
        ));

        addEffect(EffectSlot.STATIC, new GainActivatedAbilitiesOfExiledCardsEffect());
    }
}
