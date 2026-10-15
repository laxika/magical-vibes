package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastFromHandTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2488")
public class GaleWaterdeepProdigy extends Card {

    public GaleWaterdeepProdigy() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastFromHandTriggerEffect(
                new CardTypePredicate(CardType.INSTANT),
                List.of(new CastTargetInstantOrSorceryFromGraveyardEffect(
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                        false, true, new CardTypePredicate(CardType.SORCERY)))));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastFromHandTriggerEffect(
                new CardTypePredicate(CardType.SORCERY),
                List.of(new CastTargetInstantOrSorceryFromGraveyardEffect(
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                        false, true, new CardTypePredicate(CardType.INSTANT)))));
    }
}
