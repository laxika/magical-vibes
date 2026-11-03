package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "129")
public class CrescentIslandTemple extends Card {

    public CrescentIslandTemple() {
        PermanentCount shrinesYouControl =
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.SHRINE), CountScope.CONTROLLER);

        addTrigger(Triggers.enters(ObjectRelation.THIS), monkToken(CardType.CREATURE, shrinesYouControl));
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.ENCHANTMENT,
                                new PermanentHasSubtypePredicate(CardSubtype.SHRINE))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                monkToken());
    }

    private static CreateTokenEffect monkToken() {
        return new CreateTokenEffect(1, "Monk", 1, 1, CardColor.RED, List.of(CardSubtype.MONK),
                Set.of(), Set.of(), prowessEffect());
    }

    private static CreateTokenEffect monkToken(CardType primaryType, PermanentCount amount) {
        return new CreateTokenEffect(primaryType, amount, "Monk", 1, 1, CardColor.RED, null,
                List.of(CardSubtype.MONK), Set.of(), Set.of(), false, false, prowessEffect(), List.of(),
                false, false, false, 0, Set.of());
    }

    private static List<Ability> prowessEffect() {
        return List.of(
                new TriggeredAbility(Triggers.youCastSpell()
                                .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                        new BoostSelfEffect(1, 1)));
    }
}
