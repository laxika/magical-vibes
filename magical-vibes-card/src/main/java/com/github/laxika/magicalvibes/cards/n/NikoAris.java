package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsDrawnThisTurn;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedWatchedCreatureDealsDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "225")
public class NikoAris extends Card {

    public NikoAris() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, shardToken(new XValue()));

        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new MakeCreatureUnblockableEffect(),
                        new RegisterDelayedWatchedCreatureDealsDamageEffect(List.of(ReturnToHandEffect.target()))
                ),
                "+1: Up to one target creature you control can't be blocked this turn. Whenever that creature deals damage this turn, return it to its owner's hand.",
                TargetFilters.creatureYouControl(),
                +1, null, null,
                List.of(), 0, 1
        ));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new DealDamageToTargetCreatureEffect(new Scaled(new CardsDrawnThisTurn(), 2))),
                "−1: Niko Aris deals 2 damage to target tapped creature for each card you've drawn this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsTappedPredicate(),
                        "Target must be a tapped creature"
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(shardToken(new Fixed(1))),
                "−1: Create a Shard token."
        ));
    }

    private static CreateTokenEffect shardToken(DynamicAmount amount) {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT,
                amount,
                "Shard",
                0,
                0,
                null,
                null,
                List.of(CardSubtype.SHARD),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(),
                List.of(new ActivatedAbility(
                        false,
                        "{2}",
                        List.of(new SacrificeSelfCost(), new ScryEffect(1), new DrawCardEffect(1)),
                        "{2}, Sacrifice this token: Scry 1, then draw a card."
                )),
                false,
                false,
                false,
                0,
                Set.of()
        );
    }
}
