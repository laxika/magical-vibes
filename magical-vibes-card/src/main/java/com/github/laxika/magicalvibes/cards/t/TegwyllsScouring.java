package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.TapUntappedPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOC", collectorNumber = "16")
@CardRegistration(set = "WOC", collectorNumber = "52")
public class TegwyllsScouring extends Card {

    public TegwyllsScouring() {
        addCastingOption(new AlternateHandCast(List.of(
                new ManaCastingCost("{4}{B}{B}"),
                new TapUntappedPermanentsCost(3, new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasKeywordPredicate(Keyword.FLYING))))), null, true));

        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(3, "Faerie Rogue", 1, 1, CardColor.BLACK,
                List.of(CardSubtype.FAERIE, CardSubtype.ROGUE), Set.of(Keyword.FLYING), Set.of()));
    }
}
