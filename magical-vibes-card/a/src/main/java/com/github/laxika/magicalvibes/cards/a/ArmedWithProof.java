package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MKC", collectorNumber = "9")
@CardRegistration(set = "MKC", collectorNumber = "320")
public class ArmedWithProof extends Card {

    public ArmedWithProof() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofClueToken(2));

        PermanentHasSubtypePredicate clues = new PermanentHasSubtypePredicate(CardSubtype.CLUE);
        addEffect(EffectSlot.STATIC,
                new GrantSubtypeEffect(CardSubtype.EQUIPMENT, GrantScope.OWN_PERMANENTS, false, clues));
        addEffect(EffectSlot.STATIC,
                new GrantEffectEffect(
                        new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE),
                        GrantScope.OWN_PERMANENTS,
                        clues));
        addEffect(EffectSlot.STATIC,
                new GrantActivatedAbilityEffect(new EquipActivatedAbility("{2}"), GrantScope.OWN_PERMANENTS, clues));
    }
}
