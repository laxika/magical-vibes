package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongOwnedCommandersInPlay;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnFlashbackCostEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentChoosesCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "MIC", collectorNumber = "34")
@CardRegistration(set = "MIC", collectorNumber = "72")
public class VisionsOfDread extends Card {

    public VisionsOfDread() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addSpellEffect(new TargetOpponentChoosesCardFromGraveyardEffect(new CardTypePredicate(CardType.CREATURE)));

        addEffect(EffectSlot.STATIC, new ReduceOwnFlashbackCostEffect(new GreatestManaValueAmongOwnedCommandersInPlay()));
        addCastingOption(new FlashbackCast("{8}{B}{B}"));
    }
}
