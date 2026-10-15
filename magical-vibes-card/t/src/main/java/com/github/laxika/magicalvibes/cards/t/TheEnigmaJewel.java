package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LocusOfEnlightenment;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.CraftMaterialCost;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromExileTransformedEffect;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "55")
@CardRegistration(set = "LCI", collectorNumber = "362")
public class TheEnigmaJewel extends Card {

    public TheEnigmaJewel() {
        setBackFaceCard(new LocusOfEnlightenment());

        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(ManaColor.COLORLESS, 2, new ManaRestriction.Abilities())),
                "{T}: Add {C}{C}. Spend this mana only to activate abilities."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{8}{U}",
                List.of(new ExileSelfCost(), CraftMaterialCost.nonlandsWithActivatedAbilities(4),
                        new ReturnSourceFromExileTransformedEffect()),
                "{8}{U}, Exile this artifact, Exile four or more nonlands with activated abilities from among other permanents you control and/or cards in your graveyard: Return this card transformed under its owner's control. Craft only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "LocusOfEnlightenment";
    }
}
