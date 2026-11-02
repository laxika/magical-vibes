package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "35")
public class TobyBeastieBefriender extends Card {

    public TobyBeastieBefriender() {
        PermanentPredicate creatureToken = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), new PermanentIsTokenPredicate()));

        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                1, "Beast", 4, 4, CardColor.WHITE, List.of(CardSubtype.BEAST), Set.of(), Set.of(),
                List.of(StaticAbility.of(new CantAttackOrBlockAloneEffect()))));
        addStaticEffect(new ConditionalEffect(
                new ControlsPermanentCount(4, creatureToken),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.ALL_OWN_CREATURES, creatureToken)));
    }
}
