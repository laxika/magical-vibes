package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.condition.CastNotFromHand;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "63")
public class OtterballAntics extends Card {

    public OtterballAntics() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new CastFromZone(Zone.HAND), otterToken(0)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new CastNotFromHand(), otterToken(1)));
        addCastingOption(new FlashbackCast("{3}{U}"));
    }

    private CreateTokenEffect otterToken(int initialCounters) {
        Map<EffectSlot, CardEffect> tokenEffects = Map.of(
                EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        List.of(new BoostSelfEffect(1, 1))));
        return new CreateTokenEffect(
                CardType.CREATURE, 1, "Otter", 1, 1,
                CardColor.BLUE, Set.of(CardColor.BLUE, CardColor.RED), List.of(CardSubtype.OTTER),
                Set.of(), Set.of(), false, false, tokenEffects, List.of(),
                false, false, false, initialCounters, Set.of());
    }
}
