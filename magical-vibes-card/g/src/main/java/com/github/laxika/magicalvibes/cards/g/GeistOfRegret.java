package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.PutRandomCardFromLibraryIntoGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;

@CardRegistration(set = "YMID", collectorNumber = "16")
public class GeistOfRegret extends Card {

    public GeistOfRegret() {
        CardTypePredicate instant = new CardTypePredicate(CardType.INSTANT);
        CardTypePredicate sorcery = new CardTypePredicate(CardType.SORCERY);
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(instant, sorcery));

        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new PutRandomCardFromLibraryIntoGraveyardEffect(instant),
                new PutRandomCardFromLibraryIntoGraveyardEffect(sorcery)));
        addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery).castFrom(Zone.GRAVEYARD),
                new CopyTriggeringSpellEffect());
    }
}
