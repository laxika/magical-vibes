package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;

@CardRegistration(set = "FIN", collectorNumber = "156")
@CardRegistration(set = "FIN", collectorNumber = "389")
@CardRegistration(set = "FIN", collectorNumber = "534")
public class SeiferAlmasy extends Card {

    public SeiferAlmasy() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new ConditionalEffect(new AttacksAlone(),
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET)));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new CastTargetInstantOrSorceryFromGraveyardEffect(
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                        true,
                        true,
                        new CardMaxManaValuePredicate(3)));
    }
}
