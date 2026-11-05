package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CraftMaterialCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromExileTransformedEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "60")
public class InvertedIceberg extends Card {

    public InvertedIceberg() {
        setBackFaceCard(new IcebergTitan());

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MillEffect(1, MillRecipient.CONTROLLER),
                new DrawCardEffect(1));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{U}{U}",
                List.of(new ExileSelfCost(), new CraftMaterialCost(),
                        new ReturnSourceFromExileTransformedEffect()),
                "{4}{U}{U}, Exile this artifact, Exile another artifact you control or an artifact card from your graveyard: Return this card transformed under its owner's control. Craft only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "IcebergTitan";
    }
}
