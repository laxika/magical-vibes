package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseActivatedAbilityCostEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "27")
@CardRegistration(set = "WOC", collectorNumber = "80")
public class TitheTaker extends Card {

    public TitheTaker() {
        addStaticEffect(new ConditionalEffect(new ControllerTurn(),
                new IncreaseSpellCostEffect(new CardTruePredicate(), 1, CostModificationScope.OPPONENT)));
        addStaticEffect(new ConditionalEffect(new ControllerTurn(),
                IncreaseActivatedAbilityCostEffect.opponentNonMana(new PermanentTruePredicate(), 1)));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenEffect(
                1, "Spirit", 1, 1, CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLACK), List.of(CardSubtype.SPIRIT),
                Set.of(Keyword.FLYING), Set.of()));
    }
}
