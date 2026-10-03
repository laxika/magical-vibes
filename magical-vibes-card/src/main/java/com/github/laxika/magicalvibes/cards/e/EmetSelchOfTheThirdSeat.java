package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "FIC", collectorNumber = "81")
@CardRegistration(set = "FIC", collectorNumber = "170")
@CardRegistration(set = "FIC", collectorNumber = "473")
public class EmetSelchOfTheThirdSeat extends Card {

    public EmetSelchOfTheThirdSeat() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardTruePredicate(), 2, CostModificationScope.SELF, Zone.GRAVEYARD));

        addEffect(EffectSlot.ON_OPPONENT_LOSES_LIFE, new OncePerTurnTriggerEffect(
                new CastTargetInstantOrSorceryFromGraveyardEffect(
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, true)));
    }
}
