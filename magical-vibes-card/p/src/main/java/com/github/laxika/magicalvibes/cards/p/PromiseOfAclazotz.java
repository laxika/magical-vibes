package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.FoulRebirth;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1, nonDemonCreature, Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new PopulateEffect())),
                "Sacrifice a non-Demon creature?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "FoulRebirth";
    }
}
