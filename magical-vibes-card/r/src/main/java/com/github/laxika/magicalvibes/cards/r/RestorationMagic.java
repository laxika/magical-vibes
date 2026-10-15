package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "30")
public class RestorationMagic extends Card {

    public RestorationMagic() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Cure — Target permanent gains hexproof and indestructible until end of turn",
                        List.of(new GrantKeywordEffect(Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE), GrantScope.TARGET)),
                        TargetFilters.permanent()).withManaCost("{W}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Cura — Target permanent gains hexproof and indestructible until end of turn. You gain 3 life",
                        List.of(
                                new GrantKeywordEffect(Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE), GrantScope.TARGET),
                                new GainLifeEffect(3)),
                        TargetFilters.permanent()).withManaCost("{1}{W}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Curaga — Permanents you control gain hexproof and indestructible until end of turn. You gain 6 life",
                        List.of(
                                new GrantKeywordEffect(Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE), GrantScope.OWN_PERMANENTS),
                                new GainLifeEffect(6))).withManaCost("{3}{W}{W}")
        )));
    }
}
