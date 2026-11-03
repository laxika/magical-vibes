package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "136")
public class PinkHorror extends Card {

    private static final CardPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)));

    public PinkHorror() {
        addTrigger(Triggers.youCastSpell().withFilter(INSTANT_OR_SORCERY), new DealDamageToAnyTargetEffect(2));

        List<Ability> blueHorrorEffects = List.of(
                new TriggeredAbility(Triggers.youCastSpell().withFilter(INSTANT_OR_SORCERY),
                        new DealDamageToAnyTargetEffect(1)));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenEffect(
                CardType.CREATURE, 2, "Blue Horror", 2, 2,
                CardColor.BLUE, Set.of(CardColor.BLUE, CardColor.RED),
                List.of(CardSubtype.DEMON, CardSubtype.HORROR), Set.of(), Set.of(),
                false, false, blueHorrorEffects, List.of(),
                false, false, false, 0, Set.of()));
    }
}
