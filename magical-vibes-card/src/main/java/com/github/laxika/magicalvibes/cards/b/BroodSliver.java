package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LGN", collectorNumber = "120")
@CardRegistration(set = "H09", collectorNumber = "22")
public class BroodSliver extends Card {

    public BroodSliver() {
        MayEffect createToken = new MayEffect(
                new CreateTokenForTriggeringPlayerEffect(new CreateTokenEffect(
                        "Sliver", 1, 1, null, List.of(CardSubtype.SLIVER), Set.of(), Set.of())),
                "Create a 1/1 colorless Sliver creature token?",
                null,
                MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER);
        addEffect(EffectSlot.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.SLIVER), createToken));
        addEffect(EffectSlot.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OPPONENT,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.SLIVER), createToken));
    }
}
