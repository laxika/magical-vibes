package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardWithConditionalEffectsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "38")
@CardRegistration(set = "SOC", collectorNumber = "86")
public class FeralAppetite extends Card {

    public FeralAppetite() {
        var attackingPest = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.PEST),
                new PermanentIsAttackingPredicate()));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1, 0, Set.of(Keyword.DEATHTOUCH), GrantScope.ALL_OWN_CREATURES, attackingPest));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new ExileTargetCardFromGraveyardWithConditionalEffectsEffect(
                        new CardTypePredicate(CardType.CREATURE), pestToken(), new LoseLifeEffect(0))),
                "{1}{G}: Exile target card from a graveyard. If a creature card is exiled this way, create a 1/1 black and green Pest creature token with \"When this token dies, you gain 1 life.\""
        ));
    }

    private static CreateTokenEffect pestToken() {
        return new CreateTokenEffect(
                CardType.CREATURE, 1, "Pest", 1, 1,
                CardColor.BLACK, Set.of(CardColor.BLACK, CardColor.GREEN),
                List.of(CardSubtype.PEST), Set.of(), Set.of(),
                false, false,
                Map.of(EffectSlot.ON_DEATH, new GainLifeEffect(1)),
                List.of(), false, false, false, 0, Set.of());
    }
}
