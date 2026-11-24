package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GentlemansRise;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowCastTargetCardFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIC", collectorNumber = "83")
@CardRegistration(set = "FIC", collectorNumber = "173")
public class HildibrandManderville extends Card {

    public HildibrandManderville() {
        setBackFaceCard(new GentlemansRise());
        addCastingOption(new AdventureCast("{2}{B}"));
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.otherCreaturesYouControl(),
                new PermanentIsTokenPredicate()));
        // The "may" is the later cast; the permission itself is granted when the trigger resolves.
        addTrigger(Triggers.dies(ObjectRelation.THIS), AllowCastTargetCardFromGraveyardThisTurnEffect.of(Subjects.sourceCard(), false)
                        .lasting(Duration.UNTIL_END_OF_YOUR_NEXT_TURN).asAdventure());
    }

    @Override
    public String getBackFaceClassName() {
        return "GentlemansRise";
    }
}
