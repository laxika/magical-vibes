package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "45")
public class NurglesRot extends Card {

    public NurglesRot() {
        CreateTokenEffect plaguebearer = new CreateTokenEffect(
                "Plaguebearer of Nurgle", 1, 3, CardColor.BLACK,
                List.of(CardSubtype.DEMON), Set.of(), Set.of());

        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                        SequenceEffect.of(new ReturnSourceCardFromGraveyardToOwnerHandEffect(), plaguebearer));
    }
}
