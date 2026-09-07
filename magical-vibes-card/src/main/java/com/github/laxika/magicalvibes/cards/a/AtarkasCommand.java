package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantGainLifeThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "213")
public class AtarkasCommand extends Card {

    public AtarkasCommand() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Your opponents can't gain life this turn",
                        new OpponentsCantGainLifeThisTurnEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Atarka's Command deals 3 damage to each opponent",
                        new DealDamageToPlayersEffect(3, DamageRecipient.EACH_OPPONENT)),
                new ChooseOneEffect.ChooseOneOption(
                        "You may put a land card from your hand onto the battlefield",
                        new MayEffect(
                                new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), "land"),
                                "Put a land card from your hand onto the battlefield?")),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +1/+1 and gain reach until end of turn",
                        List.of(
                                new BoostAllOwnCreaturesEffect(1, 1),
                                new GrantKeywordEffect(Keyword.REACH, GrantScope.OWN_CREATURES)))
        ), 2));
    }
}
