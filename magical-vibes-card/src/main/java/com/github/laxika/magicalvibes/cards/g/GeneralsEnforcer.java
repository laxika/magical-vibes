package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardCreateTokenIfCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "188")
public class GeneralsEnforcer extends Card {

    public GeneralsEnforcer() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                0,
                0,
                Set.of(Keyword.INDESTRUCTIBLE),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN)))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}{B}",
                List.of(new ExileGraveyardCardCreateTokenIfCreatureEffect(
                        new CreateTokenEffect(
                                "Human Soldier",
                                1,
                                1,
                                CardColor.WHITE,
                                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER),
                                Set.of(),
                                Set.of()))),
                "{2}{W}{B}: Exile target card from a graveyard. If it was a creature card, create a 1/1 white Human Soldier creature token."
        ));
    }
}
