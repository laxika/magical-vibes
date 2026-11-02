package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.i.IsiluCarrierOfTwilight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "13")
@CardRegistration(set = "ECL", collectorNumber = "286")
public class EirduCarrierOfDawn extends Card {

    public EirduCarrierOfDawn() {
        setBackFaceCard(new IsiluCarrierOfTwilight());

        // Creature spells you cast have convoke.
        addStaticEffect(new GrantSpellCastingAbilityToSpellsEffect(Keyword.CONVOKE, new CardTypePredicate(CardType.CREATURE)));

        // At the beginning of your first main phase, you may pay {B}. If you do, transform Eirdu.
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                new MayPayManaEffect("{B}", new TransformSelfEffect(),
                        "Pay {B} to transform Eirdu?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "IsiluCarrierOfTwilight";
    }
}
