package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PowerBoostForCrewAndSaddleEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "157")
public class DefendTheRider extends Card {

    public DefendTheRider() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target permanent you control gains hexproof and indestructible until end of turn",
                        new GrantKeywordEffect(
                                Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE), GrantScope.TARGET),
                        TargetFilters.permanentYouControl()),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 1/1 colorless Pilot creature token with \"This token saddles Mounts and crews Vehicles as though its power were 2 greater.\"",
                        new CreateTokenEffect(
                                1,
                                "Pilot",
                                1,
                                1,
                                null,
                                List.of(CardSubtype.PILOT),
                                Set.of(),
                                Set.of(),
                                Map.of(EffectSlot.STATIC, new PowerBoostForCrewAndSaddleEffect(2)))
                ))));
    }
}
