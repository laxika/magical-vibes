package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongOwnedCommandersInPlay;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnFlashbackCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "182")
@CardRegistration(set = "MIC", collectorNumber = "32")
@CardRegistration(set = "MIC", collectorNumber = "70")
public class VisionsOfGlory extends Card {

    public VisionsOfGlory() {
        addSpellEffect(new CreateTokenEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER),
                "Human", 1, 1, CardColor.WHITE, List.of(CardSubtype.HUMAN), Set.of(), Set.of()));
        addEffect(EffectSlot.STATIC, new ReduceOwnFlashbackCostEffect(new GreatestManaValueAmongOwnedCommandersInPlay()));
        addCastingOption(new FlashbackCast("{8}{W}{W}"));
    }
}
