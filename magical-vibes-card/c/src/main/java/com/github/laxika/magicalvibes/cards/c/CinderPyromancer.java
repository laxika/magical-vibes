package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "50")
@CardRegistration(set = "DPA", collectorNumber = "41")
@CardRegistration(set = "PD2", collectorNumber = "9")
public class CinderPyromancer extends Card {

    public CinderPyromancer() {
        // {T}: This creature deals 1 damage to target player or planeswalker.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(DealDamageEffect.toTargetPlayerOrPlaneswalker(1)),
                "{T}: This creature deals 1 damage to target player or planeswalker."));

        // Whenever you cast a red spell, you may untap this creature.
        addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.RED)),
                new MayEffect(new UntapPermanentsEffect(Subjects.source()), "Untap Cinder Pyromancer?"));
    }
}
