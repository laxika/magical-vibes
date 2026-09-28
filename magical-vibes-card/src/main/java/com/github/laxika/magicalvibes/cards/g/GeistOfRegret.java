package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.PutRandomCardFromLibraryIntoGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "16")
public class GeistOfRegret extends Card {

    public GeistOfRegret() {
        CardTypePredicate instant = new CardTypePredicate(CardType.INSTANT);
        CardTypePredicate sorcery = new CardTypePredicate(CardType.SORCERY);
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(instant, sorcery));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new PutRandomCardFromLibraryIntoGraveyardEffect(instant),
                new PutRandomCardFromLibraryIntoGraveyardEffect(sorcery)));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new CopyControllerCastSpellOnSpellCastEffect(instantOrSorcery, Zone.GRAVEYARD));
    }
}
