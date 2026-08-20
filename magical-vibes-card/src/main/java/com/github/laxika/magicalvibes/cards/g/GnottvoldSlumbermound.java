package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "258")
public class GnottvoldSlumbermound extends Card {

    public GnottvoldSlumbermound() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{R}{G}{G}",
                List.of(
                        new SacrificeSelfCost(),
                        new DestroyTargetPermanentEffect(new PermanentIsLandPredicate()),
                        new CreateTokenEffect(
                                1,
                                "Troll Warrior",
                                4,
                                4,
                                CardColor.GREEN,
                                List.of(CardSubtype.TROLL, CardSubtype.WARRIOR),
                                Set.of(Keyword.TRAMPLE),
                                Set.of(),
                                Map.of())
                ),
                "{3}{R}{G}{G}, {T}, Sacrifice Gnottvold Slumbermound: Destroy target land. Create a 4/4 green Troll Warrior creature token with trample."
        ));
    }
}
