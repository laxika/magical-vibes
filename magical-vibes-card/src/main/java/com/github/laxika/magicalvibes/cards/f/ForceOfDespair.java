package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "92")
public class ForceOfDespair extends Card {

    public ForceOfDespair() {
        // If it's not your turn, you may exile a black card from your hand rather than pay this
        // spell's mana cost.
        addCastingOption(new AlternateHandCast(
                List.of(new ExileCardsFromHandCastingCost(new CardColorPredicate(CardColor.BLACK), "black")),
                new NotControllerTurn(),
                false));

        // Destroy all creatures that entered the battlefield this turn.
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentEnteredBattlefieldThisTurnPredicate()
        ))));
    }
}
