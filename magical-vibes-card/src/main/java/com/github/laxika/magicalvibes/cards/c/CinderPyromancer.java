package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "50")
public class CinderPyromancer extends Card {

    public CinderPyromancer() {
        // {T}: This creature deals 1 damage to target player or planeswalker.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)),
                "{T}: This creature deals 1 damage to target player or planeswalker."));

        // Whenever you cast a red spell, you may untap this creature.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.RED),
                        List.of(new UntapPermanentsEffect(TapUntapScope.SELF))),
                "Untap Cinder Pyromancer?"));
    }
}
