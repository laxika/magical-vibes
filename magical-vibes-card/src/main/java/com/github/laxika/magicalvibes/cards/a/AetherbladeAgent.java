package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GitaxianMindstinger;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "88")
public class AetherbladeAgent extends Card {

    public AetherbladeAgent() {
        setBackFaceCard(new GitaxianMindstinger());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{U/P}",
                List.of(new TransformSelfEffect()),
                "{4}{U/P}: Transform this creature. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "GitaxianMindstinger";
    }
}
