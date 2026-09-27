package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "21")
@CardRegistration(set = "MOC", collectorNumber = "108")
public class DeluxeDragster extends Card {

    public DeluxeDragster() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"
        ));

        addEffect(EffectSlot.STATIC, new CanBeBlockedOnlyByFilterEffect(
                new PermanentHasSubtypePredicate(CardSubtype.VEHICLE), "Vehicles"));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new CastTargetInstantOrSorceryFromGraveyardEffect(
                        GraveyardSearchScope.OPPONENT_GRAVEYARD, true, true));
    }
}
