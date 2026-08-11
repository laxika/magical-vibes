package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "ECL", collectorNumber = "105")
public class GrubStoriedMatriarch extends Card {

    public GrubStoriedMatriarch() {
        setBackFaceCard(new GrubNotoriousAuntie());

        ReturnCardFromGraveyardEffect returnGoblin = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardSubtypePredicate(CardSubtype.GOBLIN))
                .targetGraveyard(true)
                .upTo(true)
                .build();
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, returnGoblin);
        addEffect(EffectSlot.ON_TRANSFORM_TO_FRONT_FACE, returnGoblin);

        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new MayPayManaEffect("{R}", new TransformSelfEffect(),
                        "Pay {R} to transform Grub?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "GrubNotoriousAuntie";
    }
}
