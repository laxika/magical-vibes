package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToNAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "22")
public class ResoundingSilence extends Card {

    public ResoundingSilence() {
        // Exile target attacking creature.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsAttackingPredicate(),
                "Target must be an attacking creature"
        ))
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());

        // Cycling {5}{G}{W}{U} ({5}{G}{W}{U}, Discard this card: Draw a card.) — discard cost is intrinsic.
        // "When you cycle this card, exile up to two target attacking creatures." The reflexive trigger
        // rides on the cycling ability: the exile choice resolves first, then the cycling draw.
        addHandActivatedAbility(new ActivatedAbility(false, "{5}{G}{W}{U}",
                List.of(new ExileUpToNAttackingCreaturesEffect(2), new DrawCardEffect(1)),
                "Cycling {5}{G}{W}{U} ({5}{G}{W}{U}, Discard this card: Draw a card.)"));
    }
}
