package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.FollowHim;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "31")
@CardRegistration(set = "PIP", collectorNumber = "375")
@CardRegistration(set = "PIP", collectorNumber = "559")
@CardRegistration(set = "PIP", collectorNumber = "903")
public class JamesWanderingDad extends Card {

    public JamesWanderingDad() {
        setBackFaceCard(new FollowHim());
        addCastingOption(new AdventureCast("{X}{U}{U}"));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.COLORLESS, 2, new ManaRestriction.Abilities())),
                "{T}: Add {C}{C}. Spend this mana only to activate abilities."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "FollowHim";
    }
}
