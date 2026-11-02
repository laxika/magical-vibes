package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CastSpellsExiledWithSourceWithinTotalManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringSpellWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "19")
public class RodOfAbsorption extends Card {

    public RodOfAbsorption() {
        addTrigger(Triggers.playerCastsSpell(),
                new SpellCastTriggerEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))),
                        List.of(new ExileTriggeringSpellWithSourceEffect())));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(new SacrificeSelfCost(), new CastSpellsExiledWithSourceWithinTotalManaValueEffect()),
                "{X}, {T}, Sacrifice this artifact: You may cast any number of spells from among cards exiled "
                        + "with this artifact with total mana value X or less without paying their mana costs."));
    }
}
