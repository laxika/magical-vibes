package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "119")
public class TrickeryCharm extends Card {

    public TrickeryCharm() {
        PermanentPredicateTargetFilter creature = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature.");
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gains flying until end of turn",
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET), creature),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature becomes the creature type of your choice until end of turn",
                        new TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect(), creature),
                new ChooseOneEffect.ChooseOneOption(
                        "Look at the top four cards of your library, then put them back in any order",
                        new ReorderTopCardsOfLibraryEffect(4, LibraryOwner.CONTROLLER))
        )));
    }
}
