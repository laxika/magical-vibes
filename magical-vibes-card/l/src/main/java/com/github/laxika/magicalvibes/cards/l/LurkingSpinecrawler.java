package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellColorCount;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualIncorporationEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesCreatureUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YMKM", collectorNumber = "10")
public class LurkingSpinecrawler extends Card {

    public LurkingSpinecrawler() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ChooseCardFromHandAndApplyPerpetualIncorporationEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                        "{1}{B}",
                        new EachOpponentSacrificesCreatureUnlessDiscardEffect()));

        addTrigger(Triggers.youCastSpell().withFilter(new CardIsMulticoloredPredicate()),
                new LoseLifeEffect(new TriggeringSpellColorCount(), LoseLifeRecipient.EACH_OPPONENT, true));
    }
}
