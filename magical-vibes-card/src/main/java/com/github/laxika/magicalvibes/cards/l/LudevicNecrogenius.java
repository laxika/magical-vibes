package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "233")
public class LudevicNecrogenius extends Card {

    public LudevicNecrogenius() {
        setBackFaceCard(new OlagLudevicsHubris());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(1, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_ATTACK, new MillEffect(1, MillRecipient.CONTROLLER));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{U}{U}{B}{B}",
                List.of(
                        new ExileXCardsFromGraveyardCost(CardType.CREATURE, true),
                        new TransformSelfEffect()),
                "{X}{U}{U}{B}{B}, Exile X creature cards from your graveyard: Transform Ludevic. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "OlagLudevicsHubris";
    }
}
