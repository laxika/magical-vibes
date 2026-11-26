package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "213")
@CardRegistration(set = "HA5", collectorNumber = "21")
@CardRegistration(set = "2X2", collectorNumber = "176")
public class AtarkasCommand extends Card {

    public AtarkasCommand() {
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Your opponents can't gain life this turn",
                        new PlayersCantGainLifeThisTurnEffect(Subjects.eachOpponent())),
                new ChooseOneEffect.ChooseOneOption(
                        "Atarka's Command deals 3 damage to each opponent",
                        new DealDamageToPlayersEffect(3, Subjects.eachOpponent())),
                new ChooseOneEffect.ChooseOneOption(
                        "You may put a land card from your hand onto the battlefield",
                        new MayEffect(
                                new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), "land"),
                                "Put a land card from your hand onto the battlefield?")),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +1/+1 and gain reach until end of turn",
                        List.of(
                                BoostEffect.creaturesYouControl(1, 1),
                                new GrantKeywordEffect(Keyword.REACH, Subjects.otherCreaturesYouControl())))
        ), 2));
    }
}
