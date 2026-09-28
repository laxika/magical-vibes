package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "59")
public class KoboldWarcaller extends Card {

    public KoboldWarcaller() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ChooseCardFromHandAndApplyPerpetualKeywordEffect(
                        new CardTypePredicate(CardType.CREATURE), Set.of(Keyword.HASTE))),
                "{T}: Choose a creature card in your hand. It perpetually gains haste."
        ));
    }
}
