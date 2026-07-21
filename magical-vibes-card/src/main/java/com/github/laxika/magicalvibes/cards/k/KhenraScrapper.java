package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "HOU", collectorNumber = "100")
public class KhenraScrapper extends Card {

    public KhenraScrapper() {
        // Menace is a static keyword loaded from Scryfall. Exert: "You may exert this creature as it
        // attacks. When you do, it gets +2/+0 until end of turn." Modeled as an optional attack trigger
        // (matching Hooded Brawler). Choosing to exert also keeps the creature tapped through its next
        // untap step.
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new BoostSelfEffect(2, 0),
                        new SkipNextUntapEffect(TapUntapScope.SELF)
                ),
                "Exert Khenra Scrapper as it attacks? (It gets +2/+0 until end of turn.)"
        ));
    }
}
