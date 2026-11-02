package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "99")
public class LobberCrew extends Card {

    public LobberCrew() {
        // {T}: This creature deals 1 damage to each opponent.
        addActivatedAbility(new ActivatedAbility(true, null, List.of(
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)),
                "{T}: This creature deals 1 damage to each opponent."));

        // Whenever you cast a multicolored spell, untap this creature.
        addTrigger(Triggers.youCastSpell(),
                new SpellCastTriggerEffect(new CardIsMulticoloredPredicate(),
                        List.of(new UntapPermanentsEffect(TapUntapScope.SELF))));
    }
}
