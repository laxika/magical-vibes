package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YBLB", collectorNumber = "25")
public class IndrisTheHydrostaticSurge extends Card {

    public IndrisTheHydrostaticSurge() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                ConjureCardsEffect.named("M10", "146", ConjureDestination.intoLibrary())
                        .times(4)
                        .withAbilities(List.of(new TriggeredAbility(Triggers.youCastThisSpell(),
                                CopyTriggeringSpellEffect.storm()))),
                new ShuffleLibraryEffect(false)));

        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery), new DrawCardEffect());
    }
}
