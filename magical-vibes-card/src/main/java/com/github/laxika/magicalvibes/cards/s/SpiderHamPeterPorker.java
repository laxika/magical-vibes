package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SPM", collectorNumber = "114")
@CardRegistration(set = "SPM", collectorNumber = "201")
public class SpiderHamPeterPorker extends Card {

    public SpiderHamPeterPorker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, foodToken());
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(
                        CardSubtype.SPIDER,
                        CardSubtype.BOAR,
                        CardSubtype.BAT,
                        CardSubtype.BEAR,
                        CardSubtype.BIRD,
                        CardSubtype.CAT,
                        CardSubtype.DOG,
                        CardSubtype.FROG,
                        CardSubtype.JACKAL,
                        CardSubtype.LIZARD,
                        CardSubtype.MOUSE,
                        CardSubtype.OTTER,
                        CardSubtype.RABBIT,
                        CardSubtype.RACCOON,
                        CardSubtype.RAT,
                        CardSubtype.SQUIRREL,
                        CardSubtype.TURTLE,
                        CardSubtype.WOLF))));
    }

    private static CreateTokenEffect foodToken() {
        return CreateTokenEffect.ofArtifactToken(1, "Food", List.of(CardSubtype.FOOD), List.of(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                        "{2}, {T}, Sacrifice this token: You gain 3 life."
                )
        ));
    }
}
