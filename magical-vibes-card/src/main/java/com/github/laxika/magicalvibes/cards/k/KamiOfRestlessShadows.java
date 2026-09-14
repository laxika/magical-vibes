package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "104")
public class KamiOfRestlessShadows extends Card {

    public KamiOfRestlessShadows() {
        CardAnyOfPredicate ninjaOrRogue = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.NINJA),
                new CardSubtypePredicate(CardSubtype.ROGUE)));
        CardAllOfPredicate ninjaOrRogueCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE), ninjaOrRogue));
        GraveyardSearchScope ownGraveyard = GraveyardSearchScope.CONTROLLERS_GRAVEYARD;

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return up to one target Ninja or Rogue creature card from your graveyard to your hand",
                        List.of(ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(ninjaOrRogueCreature)
                                .source(ownGraveyard)
                                .targetGraveyard(true)
                                .upTo(true)
                                .build()),
                        new GraveyardCardPredicateTargetFilter(ninjaOrRogueCreature, ownGraveyard),
                        null, 0, 1, false, null),
                new ChooseOneEffect.ChooseOneOption(
                        "Put target creature card from your graveyard on top of your library",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .source(ownGraveyard)
                                .targetGraveyard(true)
                                .build(),
                        new GraveyardCardPredicateTargetFilter(
                                new CardTypePredicate(CardType.CREATURE), ownGraveyard))
        )));
    }
}
