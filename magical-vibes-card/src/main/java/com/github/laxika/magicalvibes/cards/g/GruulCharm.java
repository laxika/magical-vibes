package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "169")
public class GruulCharm extends Card {

    public GruulCharm() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures without flying can't block this turn",
                        new CantBlockThisTurnEffect(
                                TapUntapScope.ALL_CREATURES,
                                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))
                        )
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Gain control of all permanents you own",
                        new GainControlOfAllPermanentsMatchingEffect(new PermanentOwnedBySourceControllerPredicate())
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Gruul Charm deals 3 damage to each creature with flying",
                        new MassDamageEffect(3, false, false, new PermanentHasKeywordPredicate(Keyword.FLYING))
                )
        )));
    }
}
