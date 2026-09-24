package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1444")
@CardRegistration(set = "MH1", collectorNumber = "29")
public class SisayWeatherlightCaptain extends Card {

    public SisayWeatherlightCaptain() {
        PermanentPredicate otherLegendaryPermanents = new PermanentAllOfPredicate(List.of(
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
        ColorsAmongControlledPermanents colorsAmongOtherLegendaryPermanents =
                new ColorsAmongControlledPermanents(otherLegendaryPermanents);

        addEffect(EffectSlot.STATIC, new BoostSelfEffect(
                colorsAmongOtherLegendaryPermanents, colorsAmongOtherLegendaryPermanents));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{U}{B}{R}{G}",
                List.of(new SearchLibraryEffect(new CardAllOfPredicate(List.of(
                        new CardIsPermanentPredicate(),
                        new CardSupertypePredicate(CardSupertype.LEGENDARY))),
                        LibrarySearchDestination.BATTLEFIELD,
                        new ManaValueBound(new Sum(new SourcePower(), new Fixed(-1)), false, 0))),
                "{W}{U}{B}{R}{G}: Search your library for a legendary permanent card with mana value less than Sisay's power, put that card onto the battlefield, then shuffle."
        ));
    }
}
