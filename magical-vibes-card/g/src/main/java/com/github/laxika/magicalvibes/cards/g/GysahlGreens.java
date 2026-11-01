package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "190")
public class GysahlGreens extends Card {

    public GysahlGreens() {
        CreateTokenEffect bird = new CreateTokenEffect(
                1, "Bird", 2, 2, CardColor.GREEN, List.of(CardSubtype.BIRD), Set.of(), Set.of(),
                List.of(new TriggeredAbility(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                        new BoostSelfEffect(1, 0))));
        addSpellEffect(bird);
        addCastingOption(new FlashbackCast("{6}{G}"));
    }
}
