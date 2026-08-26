package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceHasSubtype;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureTypeWithBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetNameEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfBasePowerToughnessIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfKeywordIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfPermanentSupertypeIndefinitelyEffect;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "34")
public class TenthDistrictHero extends Card {

    public TenthDistrictHero() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(
                        new CollectEvidenceCost(2),
                        new BecomeCreatureTypeWithBasePowerToughnessEffect(4, 4, CardSubtype.DETECTIVE),
                        new SetSelfKeywordIndefinitelyEffect(Keyword.VIGILANCE, true)
                ),
                "{1}{W}, Collect evidence 2: This creature becomes a Human Detective with base power and toughness 4/4 and gains vigilance."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(
                        new CollectEvidenceCost(4),
                        new ConditionalEffect(
                                new SourceHasSubtype(CardSubtype.DETECTIVE),
                                SequenceEffect.of(
                                        new SetSelfBasePowerToughnessIndefinitelyEffect(5, 5),
                                        new SetSelfPermanentSupertypeIndefinitelyEffect(CardSupertype.LEGENDARY, true),
                                        new GrantStaticEffectToSourceEffect(
                                                new SetNameEffect("Mileva, the Stalwart", GrantScope.SELF)),
                                        new GrantStaticEffectToSourceEffect(
                                                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_CREATURES))
                                )
                        )
                ),
                "{2}{W}, Collect evidence 4: If this creature is a Detective, it becomes a legendary creature named Mileva, the Stalwart, has base power and toughness 5/5, and gains \"Other creatures you control have indestructible.\""
        ));
    }
}
