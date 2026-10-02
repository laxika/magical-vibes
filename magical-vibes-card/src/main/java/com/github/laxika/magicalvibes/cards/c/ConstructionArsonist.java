package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellColorCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualIncorporationEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YMKM", collectorNumber = "15")
public class ConstructionArsonist extends Card {

    public ConstructionArsonist() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseCardFromHandAndApplyPerpetualIncorporationEffect(
                        instantOrSorcery,
                        "{R}",
                        new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT)));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardIsMulticoloredPredicate(),
                List.of(new BoostSelfEffect(
                        new TriggeringSpellColorCount(),
                        new TriggeringSpellColorCount()))));
    }
}
