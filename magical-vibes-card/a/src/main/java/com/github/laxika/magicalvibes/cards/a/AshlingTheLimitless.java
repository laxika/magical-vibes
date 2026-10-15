package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSacrificedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEvokeToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ECC", collectorNumber = "1")
public class AshlingTheLimitless extends Card {

    public AshlingTheLimitless() {
        addEffect(EffectSlot.STATIC, new GrantEvokeToSpellsEffect("{4}", new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardSubtypePredicate(CardSubtype.ELEMENTAL)))));

        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, new TriggeringPermanentConditionalEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.ELEMENTAL),
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                new CreateTokenCopyOfSacrificedPermanentEffect()));
    }
}
