package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellColorCount;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualIncorporationEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesCreatureUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YMKM", collectorNumber = "10")
public class LurkingSpinecrawler extends Card {

    public LurkingSpinecrawler() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseCardFromHandAndApplyPerpetualIncorporationEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                        "{1}{B}",
                        new EachOpponentSacrificesCreatureUnlessDiscardEffect()));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardIsMulticoloredPredicate(),
                List.of(new LoseLifeEffect(
                        new TriggeringSpellColorCount(), LoseLifeRecipient.EACH_OPPONENT, true))));
    }
}
