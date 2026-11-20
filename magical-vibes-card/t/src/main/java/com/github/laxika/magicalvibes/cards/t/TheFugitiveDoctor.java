package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "130")
@CardRegistration(set = "WHO", collectorNumber = "417")
@CardRegistration(set = "WHO", collectorNumber = "541")
@CardRegistration(set = "WHO", collectorNumber = "735")
@CardRegistration(set = "WHO", collectorNumber = "1008")
@CardRegistration(set = "WHO", collectorNumber = "1132")
public class TheFugitiveDoctor extends Card {

    public TheFugitiveDoctor() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofClueToken(1));
        addTrigger(Triggers.attacks(), new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1, new PermanentHasSubtypePredicate(CardSubtype.CLUE), Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new QueueReflexiveAbilityEffect(
                                new GrantFlashbackToTargetGraveyardCardEffect(
                                Set.of(CardType.INSTANT, CardType.SORCERY), "{2}{R}{G}")))),
                "Sacrifice a Clue?"));
    }
}
