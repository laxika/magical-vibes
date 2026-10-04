package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.EncoreEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SCD", collectorNumber = "100")
public class RakshasaDebaser extends Card {

    public RakshasaDebaser() {
        addEffect(EffectSlot.ON_ATTACK,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .source(GraveyardSearchScope.OPPONENT_GRAVEYARD)
                        .targetGraveyard(true)
                        .build());

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{6}{B}{B}",
                List.of(new ExileSelfFromGraveyardCost(), new EncoreEffect()),
                "Encore {6}{B}{B}",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
