package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HarnfelHornOfBounty;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AllowExtraBoastActivationEffect;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "123")
public class BirgiGodOfStorytelling extends Card {

    public BirgiGodOfStorytelling() {
        setBackFaceCard(new HarnfelHornOfBounty());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new AwardPersistentManaEffect(ManaColor.RED, new Fixed(1)))
        ));
        addEffect(EffectSlot.STATIC, new AllowExtraBoastActivationEffect());
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Birgi, God of Storytelling", List.of())
                        .withManaCost("{2}{R}"),
                new ChooseOneEffect.ChooseOneOption("Harnfel, Horn of Bounty", List.of())
                        .withManaCost("{4}{R}")
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "HarnfelHornOfBounty";
    }
}
