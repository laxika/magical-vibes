package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "254")
public class DawnBlessedPennant extends Card {

    private static final List<CardSubtype> CHOOSABLE_TYPES = List.of(
            CardSubtype.ELEMENTAL,
            CardSubtype.ELF,
            CardSubtype.FAERIE,
            CardSubtype.GIANT,
            CardSubtype.GOBLIN,
            CardSubtype.KITHKIN,
            CardSubtype.MERFOLK,
            CardSubtype.TREEFOLK);

    public DawnBlessedPennant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect(CHOOSABLE_TYPES));
        addEffect(EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSourceChosenSubtypePredicate(), new GainLifeEffect(1)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(), ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .sourceChosenSubtype(true)
                        .targetGraveyard(true)
                        .build()),
                "{2}, {T}, Sacrifice this artifact: Return target card of the chosen type from your graveyard to your hand."));
    }
}
