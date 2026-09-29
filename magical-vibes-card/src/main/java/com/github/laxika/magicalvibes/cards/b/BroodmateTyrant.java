package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EncoreEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "71")
@CardRegistration(set = "M3C", collectorNumber = "123")
public class BroodmateTyrant extends Card {

    public BroodmateTyrant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect("Dragon", 5, 5, CardColor.RED,
                        List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING), Set.of()));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{5}{B}{R}{G}",
                List.of(new ExileSelfFromGraveyardCost(), new EncoreEffect()),
                "Encore {5}{B}{R}{G}",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
