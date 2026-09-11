package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "124")
public class DownInTheValley extends Card {

    private static final PermanentHasSubtypePredicate ELVES =
            new PermanentHasSubtypePredicate(CardSubtype.ELF);
    private static final CreateTokenEffect ELF_TOKEN = new CreateTokenEffect(
            "Elf", 1, 1, CardColor.GREEN, List.of(CardSubtype.ELF), Set.of(), Set.of());

    public DownInTheValley() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.HAND));
        addEffect(EffectSlot.SAGA_CHAPTER_II, GrantEffectToTargetEffect.toSourcePermanent(
                EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, ELF_TOKEN));
        addBuffChapter(EffectSlot.SAGA_CHAPTER_III);
        addBuffChapter(EffectSlot.SAGA_CHAPTER_IV);
    }

    private void addBuffChapter(EffectSlot chapter) {
        addEffect(chapter, new BoostAllOwnCreaturesEffect(1, 0, ELVES));
        addEffect(chapter, new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ALL_OWN_CREATURES, ELVES));
    }
}
