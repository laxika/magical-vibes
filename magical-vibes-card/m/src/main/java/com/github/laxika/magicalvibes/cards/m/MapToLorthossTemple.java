package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LorthosTheTidemaker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CompleteChecklistObjectiveEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "295")
@CardRegistration(set = "MB2", collectorNumber = "531")
public class MapToLorthossTemple extends Card {

    private static final String DIVING_GEAR = "Diving Gear";
    private static final String MERFOLK = "Merfolk";
    private static final String RITUAL = "Ritual";
    private static final Set<String> OBJECTIVES = Set.of(DIVING_GEAR, MERFOLK, RITUAL);

    public MapToLorthossTemple() {
        CardEffect completion = new SacrificeSelfThenEffect(
                new CreateTokenCopyOfCardEffect(
                        new LorthosTheTidemaker(),
                        new CreateTokenCopyOfTargetPermanentEffect()));

        addTrigger(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL),
                new CompleteChecklistObjectiveEffect(DIVING_GEAR, OBJECTIVES, completion));
        addTrigger(Triggers.enters(null, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.MERFOLK),
                        new CompleteChecklistObjectiveEffect(MERFOLK, OBJECTIVES, completion)));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))),
                        List.of(new CompleteChecklistObjectiveEffect(RITUAL, OBJECTIVES, completion))));
    }
}
