package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "138")
public class ScarredVinebreeder extends Card {

    public ScarredVinebreeder() {
        // {2}{B}, Exile an Elf card from your graveyard: This creature gets +3/+3 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{2}{B}",
                List.of(new ExileCardFromGraveyardCost(CardSubtype.ELF), new BoostSelfEffect(3, 3)),
                "{2}{B}, Exile an Elf card from your graveyard: Scarred Vinebreeder gets +3/+3 until end of turn."
        ));
    }
}
