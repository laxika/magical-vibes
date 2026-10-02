package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "199")
public class Oliphaunt extends Card {

    public Oliphaunt() {
        PermanentPredicate anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        target(new ControlledPermanentPredicateTargetFilter(
                anotherCreature,
                "Target must be another creature you control"
        )).addEffect(EffectSlot.ON_ATTACK, new BoostTargetCreatureEffect(2, 0, anotherCreature))
                .addEffect(EffectSlot.ON_ATTACK,
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET, anotherCreature));

        addHandActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.MOUNTAIN))),
                "Mountaincycling {1} ({1}, Discard this card: Search your library for a Mountain card, "
                        + "reveal it, put it into your hand, then shuffle.)"));
    }
}
