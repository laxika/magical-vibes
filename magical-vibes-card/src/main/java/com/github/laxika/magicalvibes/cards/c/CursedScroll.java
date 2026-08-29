package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseNameRevealRandomCardFromHandDealDamageEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "281")
@CardRegistration(set = "TPR", collectorNumber = "220")
public class CursedScroll extends Card {

    public CursedScroll() {
        addActivatedAbility(new ActivatedAbility(true, "{3}",
                List.of(new ChooseNameRevealRandomCardFromHandDealDamageEffect(2)),
                "{3}, {T}: Choose a card name, then reveal a card at random from your hand. "
                        + "If that card has the chosen name, Cursed Scroll deals 2 damage to any target."));
    }
}
