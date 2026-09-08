package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "5")
public class TheBirthOfMeletis extends Card {

    public TheBirthOfMeletis() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new SearchLibraryEffect(new CardAllOfPredicate(List.of(
                new CardSupertypePredicate(CardSupertype.BASIC),
                new CardSubtypePredicate(CardSubtype.PLAINS)
        ))));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new CreateTokenEffect(
                "Wall", 0, 4, null, List.of(CardSubtype.WALL), Set.of(Keyword.DEFENDER), Set.of(CardType.ARTIFACT)
        ));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new GainLifeEffect(2));
    }
}
