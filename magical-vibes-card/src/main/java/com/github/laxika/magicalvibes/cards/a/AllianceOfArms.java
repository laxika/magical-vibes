package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaForTokensEffect;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "CMD", collectorNumber = "4")
public class AllianceOfArms extends Card {

    public AllianceOfArms() {
        addEffect(EffectSlot.SPELL, EachPlayerPaysAnyManaForTokensEffect.eachPlayerCreatesTotalMana(
                new CreateTokenEffect("Soldier", 1, 1, CardColor.WHITE, List.of(CardSubtype.SOLDIER),
                        Set.of(), Set.of())));
    }
}
