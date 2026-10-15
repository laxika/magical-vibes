package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CraftMaterialCost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromExileTransformedEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "233")
public class MastersGuideMural extends Card {

    public MastersGuideMural() {
        setBackFaceCard(new MastersManufactory());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, golemToken());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}{W}{U}",
                List.of(new ExileSelfCost(), new CraftMaterialCost(), new ReturnSourceFromExileTransformedEffect()),
                "Craft with artifact {4}{W}{W}{U} ({4}{W}{W}{U}, Exile this artifact, Exile another artifact you control "
                        + "or an artifact card from your graveyard: Return this card transformed under its owner's "
                        + "control. Craft only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    static CreateTokenEffect golemToken() {
        return new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Golem",
                4,
                4,
                null,
                Set.of(CardColor.WHITE, CardColor.BLUE),
                List.of(CardSubtype.GOLEM),
                Set.of(),
                Set.of(CardType.ARTIFACT),
                false,
                false,
                Map.of(),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of());
    }

    @Override
    public String getBackFaceClassName() {
        return "MastersManufactory";
    }
}
