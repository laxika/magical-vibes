package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongOwnedCommanders;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "182")
public class VisionsOfGlory extends Card {

    public VisionsOfGlory() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER),
                "Human", 1, 1, CardColor.WHITE, List.of(CardSubtype.HUMAN), Set.of(), Set.of()));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new CastFromZone(Zone.GRAVEYARD),
                new ReduceOwnCastCostEffect(new GreatestManaValueAmongOwnedCommanders())));
        addCastingOption(new FlashbackCast("{8}{W}{W}"));
    }
}
