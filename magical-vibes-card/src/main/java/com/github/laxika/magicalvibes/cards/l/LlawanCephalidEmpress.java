package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllPermanentsOfColorToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "42")
public class LlawanCephalidEmpress extends Card {

    public LlawanCephalidEmpress() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ReturnAllPermanentsOfColorToHandEffect(CardColor.BLUE,
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())), true));

        CardPredicate blueCreatureSpell = new CardAllOfPredicate(List.of(
                new CardColorPredicate(CardColor.BLUE),
                new CardTypePredicate(CardType.CREATURE)));
        addEffect(EffectSlot.STATIC,
                new OpponentsCantCastSpellsMatchingPredicateEffect(blueCreatureSpell));
    }
}
