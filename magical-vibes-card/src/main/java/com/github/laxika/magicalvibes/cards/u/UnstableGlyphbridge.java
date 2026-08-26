package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SandswirlWanderglyph;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureForEachPlayerDestroyRestEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CraftMaterialCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromExileTransformedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "41")
public class UnstableGlyphbridge extends Card {

    public UnstableGlyphbridge() {
        setBackFaceCard(new SandswirlWanderglyph());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new WasCast(),
                new ChooseCreatureForEachPlayerDestroyRestEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtMostPredicate(2))))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}{W}",
                List.of(new ExileSelfCost(), new CraftMaterialCost(), new ReturnSourceFromExileTransformedEffect()),
                "{3}{W}{W}, Exile this artifact, Exile another artifact you control or an artifact card from your graveyard: Return this card transformed under its owner's control. Craft only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "SandswirlWanderglyph";
    }
}
