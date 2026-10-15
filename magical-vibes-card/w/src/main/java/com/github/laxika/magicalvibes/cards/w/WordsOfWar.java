package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterNextDrawDamageReplacementEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "244")
public class WordsOfWar extends Card {

    public WordsOfWar() {
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new RegisterNextDrawDamageReplacementEffect()),
                "{1}: The next time you would draw a card this turn, this enchantment deals 2 damage to any target instead."));
    }
}
