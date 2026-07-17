package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "114")
public class Skeletonize extends Card {

    public Skeletonize() {
        // 1/1 black Skeleton token with "{B}: Regenerate this token."
        CreateTokenEffect skeletonToken = new CreateTokenEffect(
                CardType.CREATURE, 1, "Skeleton", 1, 1, CardColor.BLACK, null,
                List.of(CardSubtype.SKELETON), Set.<Keyword>of(), Set.<CardType>of(), false, false, Map.of(),
                List.of(new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()), "{B}: Regenerate this token.")),
                false, false, false, 0, Set.<Keyword>of());

        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3))
                .addEffect(EffectSlot.SPELL, new CreateTokenOnTargetDeathThisTurnEffect(skeletonToken));
    }
}
