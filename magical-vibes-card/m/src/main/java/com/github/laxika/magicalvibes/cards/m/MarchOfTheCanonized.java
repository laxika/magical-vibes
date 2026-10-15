package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.DevotionToColorsAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCC", collectorNumber = "41")
@CardRegistration(set = "LCC", collectorNumber = "73")
public class MarchOfTheCanonized extends Card {

    public MarchOfTheCanonized() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                new XValue(), "Vampire", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.VAMPIRE), Set.of(Keyword.LIFELINK), Set.of()));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new DevotionToColorsAtLeast(Set.of(ManaColor.WHITE, ManaColor.BLACK), 7),
                new CreateTokenEffect(1, "Vampire Demon", 4, 3, CardColor.WHITE,
                        Set.of(CardColor.WHITE, CardColor.BLACK),
                        List.of(CardSubtype.VAMPIRE, CardSubtype.DEMON),
                        Set.of(Keyword.FLYING), Set.of())));
    }
}
