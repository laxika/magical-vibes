package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandMayCastWithoutPayingManaEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "700")
public class NicoMinoruRunaway extends Card {

    public NicoMinoruRunaway() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT)),
                new StackEntryNotPredicate(new StackEntryCastFromZonePredicate(Zone.HAND))));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new ExileTopUntilNonlandMayCastWithoutPayingManaEffect()
                ),
                "{2}{R}, {T}, Discard a card: Exile cards from the top of your library until you exile a nonland card. You may cast that card without paying its mana cost."
        ));
    }
}
