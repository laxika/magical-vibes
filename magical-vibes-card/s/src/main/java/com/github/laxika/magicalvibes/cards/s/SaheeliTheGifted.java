package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEachOtherControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C18", collectorNumber = "44")
public class SaheeliTheGifted extends Card {

    public SaheeliTheGifted() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new CreateTokenEffect(1, "Servo", 1, 1, null,
                        List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT))),
                "+1: Create a 1/1 colorless Servo artifact creature token."
        ));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new ReduceCastCostForNextMatchingSpellEffect(
                        new CardTruePredicate(),
                        new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER))),
                "+1: The next spell you cast this turn has affinity for artifacts."
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateTokenCopyOfEachOtherControlledPermanentEffect(
                        new PermanentIsArtifactPredicate(),
                        new CreateTokenCopyOfTargetPermanentEffect(true, true))),
                "−7: For each artifact you control, create a token that's a copy of it. "
                        + "Those tokens gain haste. Exile those tokens at the beginning of the next end step."
        ));
    }
}
