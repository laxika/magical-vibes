package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "104")
@CardRegistration(set = "MSC", collectorNumber = "433")
public class TheFantasticar extends Card {

    private static final CardNotPredicate NONCREATURE_SPELL =
            new CardNotPredicate(new CardTypePredicate(CardType.CREATURE));

    private static final CreateTokenEffect CONSTRUCTS = new CreateTokenEffect(
            4,
            "Construct",
            4,
            4,
            null,
            List.of(CardSubtype.CONSTRUCT),
            Set.of(Keyword.FLYING, Keyword.HASTE),
            Set.of(CardType.ARTIFACT));

    public TheFantasticar() {
        // Whenever you cast a noncreature spell, you may have The Fantasticar become an artifact
        // creature until end of turn.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        NONCREATURE_SPELL,
                        List.of(new MayEffect(
                                AnimatePermanentsEffect.crew(),
                                "Have The Fantasticar become an artifact creature?"))));

        // Whenever you cast your fourth noncreature spell each turn, you may sacrifice The
        // Fantasticar. If you do, create four 4/4 colorless Construct artifact creature tokens
        // with flying and haste.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                SpellCastTriggerEffect.nth(
                        4,
                        NONCREATURE_SPELL,
                        List.of(new MayEffect(
                                new SacrificeSelfThenEffect(CONSTRUCTS),
                                "Sacrifice The Fantasticar and create four Constructs?"))));
    }
}
