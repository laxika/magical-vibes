package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.condition.SourceIsAttached;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastOrActivateDuringYourTurnEffect;

@CardRegistration(set = "SLD", collectorNumber = "1571")
@CardRegistration(set = "2X2", collectorNumber = "302")
@CardRegistration(set = "PZA", collectorNumber = "15")
@CardRegistration(set = "FIC", collectorNumber = "340")
@CardRegistration(set = "C16", collectorNumber = "53")
public class ConquerorsFlail extends Card {

    public ConquerorsFlail() {
        addStaticEffect(new AttachedBoostEffect(
                new ColorsAmongControlledPermanents(), new ColorsAmongControlledPermanents(),
                GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new ConditionalEffect(new SourceIsAttached(),
                new OpponentsCantCastOrActivateDuringYourTurnEffect(false)));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
