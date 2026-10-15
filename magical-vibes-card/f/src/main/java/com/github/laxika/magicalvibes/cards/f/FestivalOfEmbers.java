package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CastingCost;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.effect.CastSpellsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCardsInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "134")
public class FestivalOfEmbers extends Card {

    public FestivalOfEmbers() {
        addEffect(EffectSlot.STATIC, new CastSpellsFromGraveyardEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                List.<CastingCost>of(new LifeCastingCost(1)), true));
        addEffect(EffectSlot.STATIC, new ExileOwnCardsInsteadOfGraveyardEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new SacrificeSelfEffect()),
                "{1}{R}: Sacrifice this enchantment."
        ));
    }
}
