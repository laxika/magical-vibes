package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCC", collectorNumber = "199")
@CardRegistration(set = "VOC", collectorNumber = "18")
@CardRegistration(set = "VOC", collectorNumber = "56")
public class GlassCastHeart extends Card {

    public GlassCastHeart() {
        // Whenever one or more Vampires you control attack, create a Blood token.
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(new HasAttacker(new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE)),
                        CreateTokenEffect.ofBloodToken(1)));

        // {B}, {T}, Pay 1 life: Create a 1/1 white and black Vampire creature token with lifelink.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(
                        new PayLifeCost(1),
                        new CreateTokenEffect(1, "Vampire", 1, 1, CardColor.WHITE,
                                Set.of(CardColor.WHITE, CardColor.BLACK),
                                List.of(CardSubtype.VAMPIRE), Set.of(Keyword.LIFELINK), Set.of())
                ),
                "{B}, {T}, Pay 1 life: Create a 1/1 white and black Vampire creature token with lifelink."
        ));

        // {B}{B}, {T}, Sacrifice this artifact and thirteen Blood tokens:
        // Each opponent loses 13 life and you gain 13 life.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}{B}",
                List.of(
                        new SacrificeSelfCost(),
                        new SacrificeMultiplePermanentsCost(13, new PermanentAllOfPredicate(List.of(
                                new PermanentIsTokenPredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.BLOOD)
                        ))),
                        new LoseLifeEffect(13, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(13)
                ),
                "{B}{B}, {T}, Sacrifice this artifact and thirteen Blood tokens: Each opponent loses 13 life and you gain 13 life."
        ));
    }
}
