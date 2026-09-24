package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromOpponentGraveyardAndMayCastCopyAtTriggeringSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.MillEachOpponentThenIfMilledEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "35")
@CardRegistration(set = "HOC", collectorNumber = "75")
public class SarumanOfManyColors extends Card {

    public SarumanOfManyColors() {
        CardPredicate noncreatureSpell = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ENCHANTMENT),
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessDiscardsEffect(noncreatureSpell));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                SpellCastTriggerEffect.nth(2, null, List.of(
                        new MillEachOpponentThenIfMilledEffect(
                                2,
                                new ExileTargetCardFromOpponentGraveyardAndMayCastCopyAtTriggeringSpellManaValueEffect(
                                        noncreatureSpell, GraveyardSearchScope.OPPONENT_GRAVEYARD)))));
    }
}
