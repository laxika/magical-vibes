package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoControllerLibraryEffect;
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
        CardEffect stormLightningBolt = new ConjureCardIntoControllerLibraryEffect(
                "M10", "146", List.of(new TriggeredAbility(Triggers.youCastThisSpell(),
                        CopyTriggeringSpellEffect.storm())));
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                stormLightningBolt,
                stormLightningBolt,
                stormLightningBolt,
                stormLightningBolt,
                new ShuffleLibraryEffect(false)));

        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery), new DrawCardEffect());
    }
}
