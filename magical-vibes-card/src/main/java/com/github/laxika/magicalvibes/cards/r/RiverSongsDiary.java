package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringSpellWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastRandomCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastFromHandTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "182")
public class RiverSongsDiary extends Card {

    public RiverSongsDiary() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new SpellCastFromHandTriggerEffect(instantOrSorcery,
                        List.of(new ExileTriggeringSpellWithSourceEffect())));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayCastRandomCardExiledWithSourceEffect());
    }
}
