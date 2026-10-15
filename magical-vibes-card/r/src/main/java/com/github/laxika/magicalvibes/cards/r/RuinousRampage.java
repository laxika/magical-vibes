package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "158")
public class RuinousRampage extends Card {

    public RuinousRampage() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Ruinous Rampage deals 3 damage to each opponent",
                        new DealDamageToPlayersEffect(3, DamageRecipient.EACH_OPPONENT)),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile all artifacts with mana value 3 or less",
                        new ExileAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentMaxManaValuePredicate(3)
                        )))
        ))));
    }
}
