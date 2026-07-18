package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

@CardRegistration(set = "4ED", collectorNumber = "24")
public class ElderLandWurm extends Card {

    public ElderLandWurm() {
        // Defender and trample are keywords auto-loaded from Scryfall.
        // When this creature blocks, it loses defender (no duration — the loss lasts as long
        // as it stays on the battlefield, so it can attack on later turns).
        addEffect(EffectSlot.ON_BLOCK,
                new RemoveKeywordEffect(Keyword.DEFENDER, GrantScope.SELF, EffectDuration.PERMANENT));
    }
}
