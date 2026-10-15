package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "44")
@CardRegistration(set = "MOC", collectorNumber = "131")
public class WildfireAwakener extends Card {

    public WildfireAwakener() {
        Map<EffectSlot, CardEffect> elementalTokenEffects = Map.of(
                EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsSourceCardPredicate(),
                        new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PLAYER)));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                CardType.CREATURE, new XValue(), "Elemental", 1, 1,
                CardColor.RED, null, List.of(CardSubtype.ELEMENTAL), Set.of(), Set.of(),
                false, false, elementalTokenEffects, List.of(), false, false, false, 0, Set.of()));
    }
}
