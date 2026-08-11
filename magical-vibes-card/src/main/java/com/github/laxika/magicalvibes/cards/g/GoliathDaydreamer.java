package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringSpellWithDreamCounterEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastExiledCardsWithDreamCounterEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "143")
public class GoliathDaydreamer extends Card {

    public GoliathDaydreamer() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                List.of(new ExileTriggeringSpellWithDreamCounterEffect()),
                new StackEntryCastFromZonePredicate(Zone.HAND)));
        addEffect(EffectSlot.ON_ATTACK, new MayCastExiledCardsWithDreamCounterEffect());
    }
}
