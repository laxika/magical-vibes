package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureTypeAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasTypeChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasTypeChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1901")
@CardRegistration(set = "ECC", collectorNumber = "112")
@CardRegistration(set = "AFC", collectorNumber = "163")
@CardRegistration(set = "C17", collectorNumber = "32")
public class KindredSummons extends Card {

    public KindredSummons() {
        var creatureOfChosenType = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE), new CardHasTypeChosenThisResolutionPredicate()));
        addSpellEffect(new ChooseCreatureTypeAtResolutionEffect());
        addSpellEffect(new RevealCardsFromTopUntilEffect(Subjects.you(), creatureOfChosenType, new PermanentCount(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(), new PermanentHasTypeChosenThisResolutionPredicate())),
                        CountScope.CONTROLLER)));
        addSpellEffect(new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(creatureOfChosenType)));
        addSpellEffect(new ShuffleLibraryEffect(false));
    }
}
