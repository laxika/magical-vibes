package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.i.IsiluCarrierOfTwilight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ECL", collectorNumber = "13")
public class EirduCarrierOfDawn extends Card {

    public EirduCarrierOfDawn() {
        setBackFaceCard(new IsiluCarrierOfTwilight());

        // Creature spells you cast have convoke.
        addEffect(EffectSlot.STATIC,
                new GrantSpellCastingAbilityToSpellsEffect(Keyword.CONVOKE, new CardTypePredicate(CardType.CREATURE)));

        // At the beginning of your first main phase, you may pay {B}. If you do, transform Eirdu.
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new MayPayManaEffect("{B}", new TransformSelfEffect(),
                        "Pay {B} to transform Eirdu?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "IsiluCarrierOfTwilight";
    }
}
