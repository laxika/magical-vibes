package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "258")
public class RestlessCottage extends Card {

    private static final CreateTokenEffect FOOD_TOKEN = CreateTokenEffect.ofArtifactToken(
            1,
            "Food",
            List.of(CardSubtype.FOOD),
            List.of(new ActivatedAbility(
                    true,
                    "{2}",
                    List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                    "{2}, {T}, Sacrifice this token: You gain 3 life."
            )));

    public RestlessCottage() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.GREEN))),
                "{T}: Add {B} or {G}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}{G}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        4, 4, List.of(CardSubtype.HORROR), Set.of(),
                        Set.of(CardColor.BLACK, CardColor.GREEN))),
                "{2}{B}{G}: This land becomes a 4/4 black and green Horror creature until end of turn. "
                        + "It's still a land."
        ));
        addEffect(EffectSlot.ON_ATTACK, FOOD_TOKEN);
        addEffect(EffectSlot.ON_ATTACK, new ExileCardsFromGraveyardEffect(1, 0));
    }
}
