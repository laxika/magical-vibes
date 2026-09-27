package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.OpponentAttacksWithAtLeastCreatures;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;

import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "13")
@CardRegistration(set = "MSC", collectorNumber = "303")
public class EverettKRossHaplessAttache extends Card {

    public EverettKRossHaplessAttache() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1, 1, Set.of(Keyword.LIFELINK), GrantScope.ALL_OWN_CREATURES,
                new PermanentIsCommanderPredicate()));
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(new OpponentAttacksWithAtLeastCreatures(2, false), new DrawCardEffect()));
    }
}
