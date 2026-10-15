package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.CreatureCardPutIntoYourGraveyardThisTurn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "249")
public class RaphaelFiendishSavior extends Card {

    public RaphaelFiendishSavior() {
        // Other Demons, Devils, Imps, and Tieflings you control get +1/+1 and have lifelink.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1,
                Set.of(Keyword.LIFELINK), GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(
                        CardSubtype.DEMON,
                        CardSubtype.DEVIL,
                        CardSubtype.IMP,
                        CardSubtype.TIEFLING
                ))));

        // At the beginning of each end step, if a creature card was put into your graveyard from
        // anywhere this turn, create a 1/1 red Devil creature token with "When this token dies, it
        // deals 1 damage to any target."
        Map<EffectSlot, CardEffect> devilTokenEffects =
                Map.of(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(1));
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new CreatureCardPutIntoYourGraveyardThisTurn(),
                new CreateTokenEffect(1, "Devil", 1, 1, CardColor.RED,
                        List.of(CardSubtype.DEVIL), Set.of(), Set.of(), devilTokenEffects)));
    }
}
