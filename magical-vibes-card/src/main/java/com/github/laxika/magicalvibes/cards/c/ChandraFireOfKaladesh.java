package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SelfDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "135")
public class ChandraFireOfKaladesh extends Card {

    public ChandraFireOfKaladesh() {
        setBackFaceCard(new ChandraRoaringFlame());

        // Whenever you cast a red spell, untap Chandra, Fire of Kaladesh.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardColorPredicate(CardColor.RED),
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF))));

        // {T}: Chandra deals 1 damage to target player or planeswalker. If Chandra has dealt 3 or more
        // damage this turn, exile her, then return her to the battlefield transformed under her owner's
        // control. The damage resolves before the count, so this ping counts toward the three.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(1),
                        new ConditionalEffect(
                                new SelfDealtDamageThisTurn(3),
                                new ExileSelfAndReturnTransformedEffect())
                ),
                "{T}: Chandra, Fire of Kaladesh deals 1 damage to target player or planeswalker. "
                        + "If Chandra has dealt 3 or more damage this turn, exile her, then return her "
                        + "to the battlefield transformed under her owner's control."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "ChandraRoaringFlame";
    }
}
