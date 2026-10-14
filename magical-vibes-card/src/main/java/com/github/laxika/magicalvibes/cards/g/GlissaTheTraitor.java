package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "MBS", collectorNumber = "96")
@CardRegistration(set = "V16", collectorNumber = "5")
@CardRegistration(set = "SLD", collectorNumber = "1217")
public class GlissaTheTraitor extends Card {

    public GlissaTheTraitor() {
        CardTypePredicate artifact = new CardTypePredicate(CardType.ARTIFACT);

        target(new GraveyardCardPredicateTargetFilter(artifact, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
                .addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES,
                        new MayEffect(
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.HAND)
                                        .filter(artifact)
                                        .targetGraveyard(true)
                                        .build(),
                                "Return target artifact card from your graveyard to your hand?"));
    }
}
