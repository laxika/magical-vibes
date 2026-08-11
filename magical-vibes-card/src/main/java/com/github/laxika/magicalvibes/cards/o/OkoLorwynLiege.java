package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "61")
public class OkoLorwynLiege extends Card {

    public OkoLorwynLiege() {
        setBackFaceCard(new OkoShadowmoorScion());

        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new MayPayManaEffect("{G}", new TransformSelfEffect(), "Pay {G} to transform Oko?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new GrantKeywordEffect(Keyword.CHANGELING, GrantScope.TARGET, GrantDuration.INDEFINITE)),
                "+2: Up to one target creature gains all creature types. (This effect doesn't end.)",
                TargetFilters.creature(),
                +2,
                null,
                null,
                List.of(),
                0,
                1));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new BoostTargetCreatureEffect(-2, 0, GrantDuration.UNTIL_YOUR_NEXT_TURN)),
                "+1: Target creature gets -2/-0 until your next turn.",
                TargetFilters.creature()));
    }

    @java.lang.Override
    public String getBackFaceClassName() {
        return "OkoShadowmoorScion";
    }
}
