package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

public class RoarOfTheFifthPeople extends Card {

    public RoarOfTheFifthPeople() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new CreateTokenEffect(
                2, "Dinosaur", 3, 3, CardColor.GREEN,
                List.of(CardSubtype.DINOSAUR), Set.of(), Set.of()));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new AwardManaOfColorsEffect(List.of(
                                ManaColor.RED, ManaColor.GREEN, ManaColor.WHITE))),
                        "{T}: Add {R}, {G}, or {W}."
                ),
                GrantScope.OWN_CREATURES,
                null,
                EffectDuration.WHILE_SOURCE_REMAINS
        ));

        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.DINOSAUR)));

        addEffect(EffectSlot.SAGA_CHAPTER_IV, new GrantKeywordEffect(
                Set.of(Keyword.DOUBLE_STRIKE, Keyword.TRAMPLE),
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR)
        ));
    }
}
