package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "141")
public class CombatCalligrapher extends Card {

    public CombatCalligrapher() {
        addEffect(EffectSlot.STATIC, new CreaturesCantAttackControllerUnlessPredicateEffect(
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.INKLING)), true));

        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK_PLAYER,
                new CreateTokenForTriggeringPlayerEffect(new CreateTokenEffect(
                        CardType.CREATURE, 1, "Inkling", 2, 1, CardColor.WHITE,
                        Set.of(CardColor.WHITE, CardColor.BLACK), List.of(CardSubtype.INKLING),
                        Set.of(Keyword.FLYING), Set.of(), true, false, Map.of(), List.of(),
                        false, false, false, 0, Set.of())));
    }
}
