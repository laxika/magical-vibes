package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "143")
@CardRegistration(set = "HOC", collectorNumber = "143")
public class WoodlandWeavemaster extends Card {

    public WoodlandWeavemaster() {
        CardSubtypePredicate elf = new CardSubtypePredicate(CardSubtype.ELF);
        BoostSelfEffect boost = new BoostSelfEffect(1, 1);
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringCardConditionalEffect(elf, boost));
        addTrigger(Triggers.enters(null, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringCardConditionalEffect(
                        new CardAllOfPredicate(List.of(elf,
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))),
                        boost));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(
                        new SourcePower(), ManaSpendRestriction.SUBTYPE_SPELL_OR_ABILITY, CardSubtype.ELF, false,
                        false)),
                "{T}: Add X mana of any one color, where X is this creature's power. Spend this mana only to cast Elf spells and activate abilities of Elf sources."
        ));
    }
}
