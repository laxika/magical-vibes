package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GitaxianSpellstalker;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "151")
public class KhenraSpellspear extends Card {

    public KhenraSpellspear() {
        setBackFaceCard(new GitaxianSpellstalker());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U/P}",
                List.of(new TransformSelfEffect()),
                "{3}{U/P}: Transform this creature. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "GitaxianSpellstalker";
    }
}
