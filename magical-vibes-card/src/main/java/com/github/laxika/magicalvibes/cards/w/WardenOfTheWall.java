package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfWithStatsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.NotControllerTurnConditionalEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DKA", collectorNumber = "153")
public class WardenOfTheWall extends Card {

    public WardenOfTheWall() {
        // Warden of the Wall enters the battlefield tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));

        // During turns other than yours, Warden of the Wall is a 2/3 Gargoyle artifact creature with flying.
        addEffect(EffectSlot.STATIC, new NotControllerTurnConditionalEffect(
                new AnimateSelfWithStatsEffect(2, 3, List.of(CardSubtype.GARGOYLE), Set.of(Keyword.FLYING))));
    }
}
