package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.condition.AttackingCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "132")
public class BattleCryGoblin extends Card {

    public BattleCryGoblin() {
        PermanentPredicate goblins = new PermanentHasSubtypePredicate(CardSubtype.GOBLIN);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new BoostAllOwnCreaturesEffect(1, 0, goblins),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES, goblins)
                ),
                "{1}{R}: Goblins you control get +1/+0 and gain haste until end of turn."
        ));

        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new AttackingCreaturesTotalPowerAtLeast(6),
                new CreateTokenEffect(1, "Goblin", 1, 1, CardColor.RED, List.of(CardSubtype.GOBLIN), true)));
    }
}
