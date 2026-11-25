package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import java.util.List;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;


@CardRegistration(set = "NEO", collectorNumber = "99")
public class HidetsuguDevouringChaos extends Card {

    public HidetsuguDevouringChaos() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new SacrificeCreatureCost(), new ScryEffect(2)),
                "{B}, Sacrifice a creature: Scry 2."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(new ExileTopCardMayPlayThisTurnEffect(false),
                        new ConditionalEffect(
                                HappenedThisWay.toACard(StepKind.EXILE,
                                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                                new QueueReflexiveAbilityEffect(
                                        DealDamageEffect.toAnyTarget(AmountThisWay.exiledManaValue())))),
                "{2}{R}, {T}: Exile the top card of your library. You may play that card this turn. "
                        + "When you exile a nonland card this way, Hidetsugu deals damage equal to the "
                        + "exiled card's mana value to any target."
        ));
    }
}
