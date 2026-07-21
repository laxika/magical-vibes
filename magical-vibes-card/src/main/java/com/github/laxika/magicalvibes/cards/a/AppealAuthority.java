package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

/**
 * Appeal // Authority — front half (Appeal).
 * Sorcery — Until end of turn, target creature gains trample and gets +X/+X, where X is the number
 * of creatures you control.
 * Back half (Authority) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "HOU", collectorNumber = "152")
public class AppealAuthority extends Card {

    public AppealAuthority() {
        Authority authority = new Authority();
        authority.setSetCode(getSetCode());
        authority.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(authority);

        // Until end of turn, target creature gains trample and gets +X/+X,
        // where X is the number of creatures you control.
        PermanentCount creaturesYouControl =
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(creaturesYouControl, creaturesYouControl));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
    }

    @Override
    public String getBackFaceClassName() {
        return "Authority";
    }
}
