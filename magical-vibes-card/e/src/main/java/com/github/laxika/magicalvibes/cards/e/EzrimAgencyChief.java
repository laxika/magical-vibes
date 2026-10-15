package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "202")
@CardRegistration(set = "MKM", collectorNumber = "360")
public class EzrimAgencyChief extends Card {

    public EzrimAgencyChief() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofClueToken(2));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact"),
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Vigilance",
                                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Lifelink",
                                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Hexproof",
                                        new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF))
                        ))
                ),
                "{1}, Sacrifice an artifact: Ezrim gains your choice of vigilance, lifelink, or hexproof until end of turn."
        ));
    }
}
