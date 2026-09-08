package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "57")
public class FrostpeakYeti extends Card {

    public FrostpeakYeti() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{S}",
                List.of(new MakeCreatureUnblockableEffect(true)),
                "{1}{S}: This creature can't be blocked this turn."
        ));
    }
}
