package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CraftMaterialCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromExileTransformedEffect;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "83")
public class WaterloggedHulk extends Card {

    public WaterloggedHulk() {
        setBackFaceCard(new WatertightGondola());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MillEffect(1, MillRecipient.CONTROLLER)),
                "{T}: Mill a card."));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new ExileSelfCost(), new CraftMaterialCost(CardSubtype.ISLAND),
                        new ReturnSourceFromExileTransformedEffect()),
                "{3}{U}, Exile this artifact, Exile an Island you control or an Island card from your graveyard: "
                        + "Return this card transformed under its owner's control. Craft only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "WatertightGondola";
    }
}
