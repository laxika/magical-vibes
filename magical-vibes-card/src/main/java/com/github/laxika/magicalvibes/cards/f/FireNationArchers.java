package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLE", collectorNumber = "237")
public class FireNationArchers extends Card {

    public FireNationArchers() {
        // {5}: This creature deals 2 damage to each opponent. Create a 2/2 red Soldier creature token.
        addActivatedAbility(new ActivatedAbility(false, "{5}", List.of(
                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT),
                new CreateTokenEffect("Soldier", 2, 2, CardColor.RED,
                        List.of(CardSubtype.SOLDIER), Set.of(), Set.of())
        ), "{5}: Fire Nation Archers deals 2 damage to each opponent. Create a 2/2 red Soldier creature token."));
    }
}
