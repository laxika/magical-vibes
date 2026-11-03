package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.AllowCastTargetCardFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "31")
public class NorikaYamazakiThePoet extends Card {

    public NorikaYamazakiThePoet() {
        addTrigger(Triggers.creatureYouControlAttacks()
                        .withFilter(new PermanentAnyOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.SAMURAI),
                                new PermanentHasSubtypePredicate(CardSubtype.WARRIOR)))),
                TriggerOptions.DEFAULT.withInterveningIf(new AttacksAlone()),
                new MayEffect(
                        new AllowCastTargetCardFromGraveyardThisTurnEffect(
                                new CardTypePredicate(CardType.ENCHANTMENT),
                                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                                false),
                        "You may cast target enchantment card from your graveyard this turn."));
    }
}
