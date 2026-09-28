package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "97")
public class ProfaneTutor extends Card {

    public ProfaneTutor() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect());
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(),
                "Suspend 2—{1}{B}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(2));
    }
}
