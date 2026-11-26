package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;

@CardRegistration(set = "INV", collectorNumber = "207")
public class RootingKavu extends Card {

    public RootingKavu() {
        // "When this creature dies, you may exile it. If you do, shuffle all creature cards from your
        // graveyard into your library." The shuffle happens only when this ability itself exiled it.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(
                SequenceEffect.of(ExileGraveyardCardsEffect.cards(Subjects.sourceCard()),
                        new ConditionalEffect(HappenedThisWay.exiled(),
                                new ShuffleGraveyardIntoLibraryEffect(false, new CardTypePredicate(CardType.CREATURE)))),
                "Exile Rooting Kavu to shuffle all creature cards from your graveyard into your library?"));
    }
}
