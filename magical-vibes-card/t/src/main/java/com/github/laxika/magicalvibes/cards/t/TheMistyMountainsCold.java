package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "104")
public class TheMistyMountainsCold extends Card {

    public TheMistyMountainsCold() {
        // I, II, III, IV — Create a Treasure token. Then if you control four or more Treasures, sacrifice
        // this Saga. If you do, create a 6/6 red Dragon creature token with flying.
        CardEffect treasureThenDragon = SequenceEffect.of(
                CreateTokenEffect.ofTreasureToken(1),
                new ConditionalEffect(
                        new ControlsPermanentCount(4,
                                new PermanentHasSubtypePredicate(CardSubtype.TREASURE)),
                        new SacrificeSelfThenEffect(new CreateTokenEffect(
                                "Dragon", 6, 6, CardColor.RED,
                                List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING), Set.of()))));
        addEffect(EffectSlot.SAGA_CHAPTER_I, treasureThenDragon);
        addEffect(EffectSlot.SAGA_CHAPTER_II, treasureThenDragon);
        addEffect(EffectSlot.SAGA_CHAPTER_III, treasureThenDragon);
        addEffect(EffectSlot.SAGA_CHAPTER_IV, treasureThenDragon);
    }
}
