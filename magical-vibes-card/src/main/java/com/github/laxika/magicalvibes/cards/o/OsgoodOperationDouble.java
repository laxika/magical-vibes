package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayFromOutsideHandTriggerEffect;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "191")
@CardRegistration(set = "WHO", collectorNumber = "367")
@CardRegistration(set = "WHO", collectorNumber = "796")
@CardRegistration(set = "WHO", collectorNumber = "958")
public class OsgoodOperationDouble extends Card {

    public OsgoodOperationDouble() {
        // When you cast this spell, create a token that's a copy of it, except it isn't legendary.
        addEffect(EffectSlot.ON_SELF_CAST, new CreateTokenCopyOfSourceEffect(true, 1));

        // {T}: Add {C}. Spend this mana only to cast an artifact spell or activate an ability of an artifact.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.COLORLESS, 1, new ManaRestriction.ArtifactSpells())),
                "{T}: Add {C}. Spend this mana only to cast an artifact spell or activate an ability of an artifact."
        ));

        // Paradox — Whenever you cast a spell from anywhere other than your hand, investigate.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new PlayFromOutsideHandTriggerEffect(List.of(CreateTokenEffect.ofClueToken(1))));
    }
}
