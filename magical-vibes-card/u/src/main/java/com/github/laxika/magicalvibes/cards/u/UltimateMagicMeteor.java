package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "62")
@CardRegistration(set = "FIC", collectorNumber = "121")
public class UltimateMagicMeteor extends Card {

    public UltimateMagicMeteor() {
        var artifactOrLand = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsLandPredicate()));
        addSpellEffect(new ConditionalReplacementEffect(
                new CastFromZone(Zone.EXILE),
                DealDamageEffect.toEachCreature(7),
                SequenceEffect.of(
                        DealDamageEffect.toEachCreature(7),
                        ChoosePermanentsEffect.oneForEach(Subjects.eachOpponent(),
                                Subjects.controlledBy(Subjects.eachOpponent(), artifactOrLand),
                                "an artifact or land"),
                        new DestroyTargetPermanentEffect(Subjects.thisWay(StepKind.CHOOSE)))));
        addCastingOption(new ForetellCast("{5}{R}"));
    }
}
