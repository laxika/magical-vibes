package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "54")
public class ThundercloudElemental extends Card {

    public ThundercloudElemental() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new TapPermanentsEffect(
                        TapUntapScope.ALL_CREATURES,
                        new PermanentToughnessAtMostPredicate(2))),
                "{3}{U}: Tap all creatures with toughness 2 or less."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new RemoveKeywordEffect(
                        Keyword.FLYING,
                        GrantScope.ALL_CREATURES,
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                "{3}{U}: All other creatures lose flying until end of turn."
        ));
    }
}
