package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

/**
 * Kjeldoran Knight — {W}{W} Creature — Human Knight (1/1).
 * Banding (auto-loaded from Scryfall).
 * {1}{W}: This creature gets +1/+0 until end of turn.
 * {W}{W}: This creature gets +0/+2 until end of turn.
 */
@CardRegistration(set = "ICE", collectorNumber = "36")
public class KjeldoranKnight extends Card {

    public KjeldoranKnight() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}", List.of(new BoostSelfEffect(1, 0)),
                "{1}{W}: This creature gets +1/+0 until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{W}{W}", List.of(new BoostSelfEffect(0, 2)),
                "{W}{W}: This creature gets +0/+2 until end of turn."));
    }
}
