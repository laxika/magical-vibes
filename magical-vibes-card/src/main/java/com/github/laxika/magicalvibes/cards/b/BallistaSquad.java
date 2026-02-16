package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.AttackingOrBlockingTargetFilter;

import java.util.List;

public class BallistaSquad extends Card {

    public BallistaSquad() {
        addActivatedAbility(new ActivatedAbility(true, "{X}{W}", List.of(new DealXDamageToTargetCreatureEffect()), true, "{X}{W}, {T}: Ballista Squad deals X damage to target attacking or blocking creature.", new AttackingOrBlockingTargetFilter()));
    }
}
