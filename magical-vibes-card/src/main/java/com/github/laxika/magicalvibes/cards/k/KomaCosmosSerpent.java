package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "221")
public class KomaCosmosSerpent extends Card {

    public KomaCosmosSerpent() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());

        // At the beginning of each upkeep, create a 3/3 blue Serpent creature token named Koma's Coil.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new CreateTokenEffect(
                "Koma's Coil", 3, 3, CardColor.BLUE,
                List.of(CardSubtype.SERPENT), Set.of(), Set.of()));

        // Sacrifice another Serpent: Choose one — tap target permanent and lock its activated
        // abilities for the turn, or grant Koma indestructible until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.SERPENT),
                                "Sacrifice another Serpent"),
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new LockTargetPermanentEffect(false, false, true,
                                EffectDuration.UNTIL_END_OF_TURN, TargetPredicates.permanent())
                ),
                "Sacrifice another Serpent: Tap target permanent. Its activated abilities can't be activated this turn.",
                TargetFilters.permanent()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.SERPENT),
                                "Sacrifice another Serpent"),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)
                ),
                "Sacrifice another Serpent: Koma, Cosmos Serpent gains indestructible until end of turn."
        ));
    }
}
