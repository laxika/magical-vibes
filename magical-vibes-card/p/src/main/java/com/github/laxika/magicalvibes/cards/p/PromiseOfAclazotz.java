package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.FoulRebirth;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "52")
@CardRegistration(set = "LCC", collectorNumber = "84")
public class PromiseOfAclazotz extends Card {

    public PromiseOfAclazotz() {
        setBackFaceCard(new FoulRebirth());
        addCastingOption(new AdventureCast("{2}{B}"));

        PermanentAllOfPredicate nonDemonCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DEMON))));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new MayEffect(
                new SacrificePermanentThenEffect(
                        nonDemonCreature,
                        new PopulateEffect(),
                        "a non-Demon creature",
                        false,
                        false),
                "Sacrifice a non-Demon creature?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "FoulRebirth";
    }
}
