package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.CastCardsWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellExile;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "19")
public class RodOfAbsorption extends Card {

    public RodOfAbsorption() {
        addTrigger(Triggers.playerCastsSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)))),
                new ExileTriggeringSpellEffect(TriggeringSpellExile.TRACKED_BY_SOURCE, false));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(new SacrificeSelfCost(), CastCardsWithoutPayingManaCostEffect.anyNumber(Subjects.exiledWithSource())
                        .withTotalManaValueAtMost(new XValue())),
                "{X}, {T}, Sacrifice this artifact: You may cast any number of spells from among cards exiled "
                        + "with this artifact with total mana value X or less without paying their mana costs."));
    }
}
