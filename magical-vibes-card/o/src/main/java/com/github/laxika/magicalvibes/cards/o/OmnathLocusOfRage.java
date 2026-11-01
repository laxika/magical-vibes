package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BFZ", collectorNumber = "217")
@CardRegistration(set = "SLD", collectorNumber = "1256")
@CardRegistration(set = "EA3", collectorNumber = "17")
@CardRegistration(set = "ECC", collectorNumber = "129")
@CardRegistration(set = "OTC", collectorNumber = "236")
@CardRegistration(set = "EOC", collectorNumber = "123")
@CardRegistration(set = "ZNC", collectorNumber = "97")
public class OmnathLocusOfRage extends Card {

    private static final DealDamageToAnyTargetEffect DEATH_DAMAGE = new DealDamageToAnyTargetEffect(3);

    public OmnathLocusOfRage() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new CreateTokenEffect(
                "Elemental", 5, 5, CardColor.RED, Set.of(CardColor.RED, CardColor.GREEN),
                List.of(CardSubtype.ELEMENTAL)));
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.ELEMENTAL), DEATH_DAMAGE));
        addTrigger(Triggers.dies(ObjectRelation.THIS), DEATH_DAMAGE);
    }
}
