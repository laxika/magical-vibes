package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "C21", collectorNumber = "15")
@CardRegistration(set = "BRC", collectorNumber = "71")
public class DigsiteEngineer extends Card {

    public DigsiteEngineer() {
        PermanentCount artifactsYouControl =
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                        new CardTypePredicate(CardType.ARTIFACT),
                        List.of(new MayPayManaEffect("{2}", new CreateTokenEffect(
                                1, "Construct", 0, 0,
                                null, List.of(CardSubtype.CONSTRUCT), Set.of(), Set.of(CardType.ARTIFACT),
                                Map.of(EffectSlot.STATIC,
                                        new BoostSelfEffect(artifactsYouControl, artifactsYouControl))),
                                "Pay {2} to create a 0/0 colorless Construct artifact creature token?")),
                        (String) null));
    }
}
