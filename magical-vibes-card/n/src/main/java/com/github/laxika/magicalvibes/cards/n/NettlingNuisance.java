package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GoadSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOC", collectorNumber = "15")
@CardRegistration(set = "WOC", collectorNumber = "51")
public class NettlingNuisance extends Card {

    public NettlingNuisance() {
        CreateTokenEffect pirateToken = new CreateTokenEffect(
                1,
                "Pirate",
                4,
                2,
                CardColor.RED,
                List.of(CardSubtype.PIRATE),
                Set.of(),
                Set.of(),
                Map.of(EffectSlot.STATIC, SequenceEffect.of(
                        new CantBlockEffect(),
                        new GoadSourceCreatureEffect())));

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.FAERIE),
                        new CreateTokenForTargetPlayerEffect(pirateToken),
                        false,
                        true));
    }
}
