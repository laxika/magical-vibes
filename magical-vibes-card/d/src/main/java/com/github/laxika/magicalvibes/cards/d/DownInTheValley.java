package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "124")
@CardRegistration(set = "HOC", collectorNumber = "124")
public class DownInTheValley extends Card {

    private static final PermanentHasSubtypePredicate ELVES =
            new PermanentHasSubtypePredicate(CardSubtype.ELF);
    private static final CreateTokenEffect ELF_TOKEN = new CreateTokenEffect(
            "Elf", 1, 1, CardColor.GREEN, List.of(CardSubtype.ELF), Set.of(), Set.of());

    public DownInTheValley() {
        addTrigger(Triggers.chapter(1),
                new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.HAND));
        addTrigger(Triggers.chapter(2), new GrantEffectToTargetEffect(
                new TriggeredAbility(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), ELF_TOKEN),
                EffectDuration.PERMANENT, false, GrantScope.SELF));
        addBuffChapter(3);
        addBuffChapter(4);
    }

    private void addBuffChapter(int chapter) {
        addTrigger(Triggers.chapter(chapter),
                new BoostAllOwnCreaturesEffect(1, 0, ELVES),
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ALL_OWN_CREATURES, ELVES));
    }
}
