package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "114")
public class CoffinQueen extends Card {

    public CoffinQueen() {
        // "You may choose not to untap this creature during your untap step."
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());

        // "{2}{B}, {T}: Put target creature card from a graveyard onto the battlefield under your
        // control." The reanimated creature is linked to Coffin Queen so the exile triggers below can
        // still refer to "that creature" once the ability has finished resolving.
        addActivatedAbility(new ActivatedAbility(
                true, "{2}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .linkToSource(true)
                        .build()),
                "{2}{B}, {T}: Put target creature card from a graveyard onto the battlefield under your control."
        ));

        // "When this creature becomes untapped or you lose control of this creature, exile that
        // creature."
        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED,
                new RemoveLinkedPermanentEffect(RemoveLinkedPermanentEffect.Mode.EXILE));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new RemoveLinkedPermanentEffect(RemoveLinkedPermanentEffect.Mode.EXILE));
    }
}
