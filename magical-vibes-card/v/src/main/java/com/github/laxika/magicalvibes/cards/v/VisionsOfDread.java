package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongOwnedCommanders;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
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
        )).addEffect(EffectSlot.SPELL,
                new TargetOpponentChoosesCardFromGraveyardEffect(new CardTypePredicate(CardType.CREATURE)));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new CastFromZone(Zone.GRAVEYARD),
                new ReduceOwnCastCostEffect(new GreatestManaValueAmongOwnedCommanders())));
        addCastingOption(new FlashbackCast("{8}{B}{B}"));
    }
}
