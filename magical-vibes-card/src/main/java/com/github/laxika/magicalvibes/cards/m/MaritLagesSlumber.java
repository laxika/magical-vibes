package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "HA4", collectorNumber = "6")
public class MaritLagesSlumber extends Card {

    public MaritLagesSlumber() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(1));
        addEffect(EffectSlot.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSupertypePredicate(CardSupertype.SNOW),
                        new ScryEffect(1)));

        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(
                        new ControlsPermanentCount(10, new PermanentHasSupertypePredicate(CardSupertype.SNOW)),
                        new SacrificeSelfThenEffect(maritLageToken())));
    }

    private static CreateTokenEffect maritLageToken() {
        return new CreateTokenEffect(
                CardType.CREATURE, 1, "Marit Lage", 20, 20,
                CardColor.BLACK, null, List.of(CardSubtype.AVATAR),
                Set.of(Keyword.FLYING, Keyword.INDESTRUCTIBLE), Set.of(),
                false, false, Map.of(), List.of(), false, false,
                true, 0, Set.of());
    }
}
