package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "C21", collectorNumber = "218")
@CardRegistration(set = "ELD", collectorNumber = "327")
public class GluttonousTroll extends Card {

    public GluttonousTroll() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, foodTokens());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                "Sacrifice another nonland permanent"),
                        new BoostSelfEffect(2, 2)),
                "{1}{G}, Sacrifice another nonland permanent: This creature gets +2/+2 until end of turn."
        ));
    }

    private static CreateTokenEffect foodTokens() {
        return CreateTokenEffect.ofArtifactToken(
                new Sum(new PlayersInGame(), new Fixed(-1)), "Food", List.of(CardSubtype.FOOD), List.of(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                        "{2}, {T}, Sacrifice this token: You gain 3 life."
                )));
    }
}
