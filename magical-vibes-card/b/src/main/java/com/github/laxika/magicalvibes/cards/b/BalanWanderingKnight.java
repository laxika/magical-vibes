package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceHasAtLeastAttachedEquipment;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "16")
@CardRegistration(set = "CMM", collectorNumber = "458")
@CardRegistration(set = "C17", collectorNumber = "2")
public class BalanWanderingKnight extends Card {

    public BalanWanderingKnight() {
        addStaticEffect(new ConditionalEffect(
                new SourceHasAtLeastAttachedEquipment(2),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, Subjects.source())));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new AttachSourceEquipmentToTargetCreatureEffect(Subjects.controlledBy(Subjects.you(),
                        new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT)), Subjects.source())),
                "{1}{W}: Attach all Equipment you control to Balan."));
    }
}
