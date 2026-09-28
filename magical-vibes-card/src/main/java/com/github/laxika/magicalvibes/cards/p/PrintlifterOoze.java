package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKC", collectorNumber = "40")
@CardRegistration(set = "MKC", collectorNumber = "350")
public class PrintlifterOoze extends Card {

    public PrintlifterOoze() {
        addMorph("{3}{G}");

        PermanentCount otherCreatures = new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER, true);
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP,
                new CreateXTokenWithXCountersEffect(
                        new CreateTokenEffect(
                                "Ooze", 0, 0, CardColor.GREEN,
                                List.of(CardSubtype.OOZE), Set.of(Keyword.TRAMPLE), Set.of()),
                        otherCreatures, CounterType.PLUS_ONE_PLUS_ONE));
    }
}
