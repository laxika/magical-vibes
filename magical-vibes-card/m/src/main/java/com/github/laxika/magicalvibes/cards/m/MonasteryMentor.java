package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "28")
@CardRegistration(set = "MOM", collectorNumber = "347")
@CardRegistration(set = "FRF", collectorNumber = "20")
@CardRegistration(set = "2X2", collectorNumber = "21")
@CardRegistration(set = "TDC", collectorNumber = "125")
public class MonasteryMentor extends Card {

    public MonasteryMentor() {
        List<Ability> tokenEffects = List.of(
                new TriggeredAbility(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        List.of(new BoostSelfEffect(1, 1)))));
        CreateTokenEffect monkToken = new CreateTokenEffect(
                1, "Monk", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.MONK), Set.of(), Set.of(), tokenEffects);

        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(monkToken)));
    }
}
