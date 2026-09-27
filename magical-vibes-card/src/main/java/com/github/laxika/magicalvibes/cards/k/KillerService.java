package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaAndSacrificePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "61")
@CardRegistration(set = "NCC", collectorNumber = "161")
public class KillerService extends Card {

    public KillerService() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                CreateTokenEffect.ofArtifactToken(
                        new Sum(new PlayersInGame(), new Fixed(-1)),
                        "Food",
                        List.of(CardSubtype.FOOD),
                        List.of(new ActivatedAbility(
                                true,
                                "{2}",
                                List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                                "{2}, {T}, Sacrifice this token: You gain 3 life."
                        ))));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new MayPayManaAndSacrificePermanentEffect(
                        "{2}",
                        new PermanentIsTokenPredicate(),
                        new CreateTokenEffect(
                                "Rhino Warrior", 4, 4, CardColor.GREEN,
                                List.of(CardSubtype.RHINO, CardSubtype.WARRIOR),
                                Set.of(), Set.of()),
                        "a token"));
    }
}
