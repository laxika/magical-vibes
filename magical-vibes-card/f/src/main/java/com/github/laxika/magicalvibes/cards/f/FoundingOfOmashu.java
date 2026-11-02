package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLE", collectorNumber = "116")
public class FoundingOfOmashu extends Card {

    public FoundingOfOmashu() {
        addTrigger(Triggers.chapter(1),
                new CreateTokenEffect(2, "Ally", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.ALLY), Set.of(), Set.of()));

        addTrigger(Triggers.chapter(2), new MayEffect(
                new DiscardAndDrawCardEffect(1, 1),
                "Discard a card?"));

        addTrigger(Triggers.chapter(3), new BoostAllOwnCreaturesEffect(1, 0));
    }
}
