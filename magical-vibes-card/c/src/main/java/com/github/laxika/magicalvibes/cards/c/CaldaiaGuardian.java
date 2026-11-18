package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "56")
@CardRegistration(set = "NCC", collectorNumber = "156")
public class CaldaiaGuardian extends Card {

    public CaldaiaGuardian() {
        addCastingOption(AlternateHandCast.blitz("{2}{G}"));

        CreateTokenEffect citizenTokens = new CreateTokenEffect(2, "Citizen", 1, 1,
                CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.WHITE),
                List.of(CardSubtype.CITIZEN));
        addTrigger(Triggers.creatureDies(
                        new PermanentMinManaValuePredicate(4),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                citizenTokens);

        // Blitz (CR 702.152a): haste while the blitz cost was paid; the end-step sacrifice is
        // created as the spell resolves.
    }
}
