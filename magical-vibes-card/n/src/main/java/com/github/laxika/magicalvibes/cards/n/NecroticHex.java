package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SCD", collectorNumber = "95")
@CardRegistration(set = "FDC", collectorNumber = "118")
public class NecroticHex extends Card {

    public NecroticHex() {
        addSpellEffect(new SacrificePermanentsEffect(
                6,
                new PermanentIsCreaturePredicate(),
                SacrificeRecipient.EACH_PLAYER).withSimultaneousChoices());
        addSpellEffect(CreateTokenEffect.blackZombie(6).withTapped(true));
    }
}
