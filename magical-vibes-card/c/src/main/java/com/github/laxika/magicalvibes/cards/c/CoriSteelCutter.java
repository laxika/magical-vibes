package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.AttachMatchingEquipmentToCreatedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "103")
public class CoriSteelCutter extends Card {

    public CoriSteelCutter() {
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.attachedCreature()));

        List<Ability> monkTokenEffects = List.of(
                new TriggeredAbility(Triggers.youCastSpell()
                                .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                        BoostEffect.self(1, 1)));
        CreateTokenEffect monkToken = new CreateTokenEffect(
                CardType.CREATURE, 1, "Monk", 1, 1, CardColor.WHITE, null,
                List.of(CardSubtype.MONK), Set.of(), Set.of(), false, false,
                monkTokenEffects, List.of(), false, false, false, 0, Set.of());

        addTrigger(Triggers.youCastSpell().nthThisTurn(2),
                monkToken,
                new MayEffect(
                        new AttachMatchingEquipmentToCreatedPermanentEffect(
                                new PermanentIsSourceCardPredicate(), true),
                        "Attach Cori-Steel Cutter to the Monk token?"));
        addActivatedAbility(new EquipActivatedAbility("{1}{R}"));
    }
}
