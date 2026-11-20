package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "61")
@CardRegistration(set = "UMA", collectorNumber = "62")
@CardRegistration(set = "C21", collectorNumber = "121")
public class LivingLore extends Card {

    public LivingLore() {
        var instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        var exiledManaValue = new GreatestManaValueAmongCardsExiledWithSource();

        addStaticEffect(new ExileCardFromGraveyardOnEnterEffect(instantOrSorcery));
        addStaticEffect(new SetPowerToughnessToAmountEffect(exiledManaValue, exiledManaValue));
        addTrigger(Triggers.dealsCombatDamage(),
                new MayEffect(
                        SequenceEffect.of(new SacrificeSelfEffect(),
                                new ConditionalEffect(HappenedThisWay.sacrificed(), new MayCastCardExiledWithSourceEffect())),
                        "You may sacrifice it. If you do, you may cast the exiled card without paying its mana cost."));
    }
}
