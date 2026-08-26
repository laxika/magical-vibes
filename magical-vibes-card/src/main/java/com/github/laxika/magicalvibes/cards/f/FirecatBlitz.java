package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.SacrificeXPermanentsCastingCost;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "JUD", collectorNumber = "88")
public class FirecatBlitz extends Card {

    public FirecatBlitz() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE, new XValue(), "Elemental Cat", 1, 1,
                CardColor.RED, null, List.of(CardSubtype.ELEMENTAL, CardSubtype.CAT),
                Set.of(Keyword.HASTE), Set.of(), false, false, Map.of(), List.of(),
                false, true, false, 0, Set.of()));
        addCastingOption(new FlashbackCast(List.of(
                new ManaCastingCost("{R}{R}"),
                new SacrificeXPermanentsCastingCost(new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)))));
    }
}
