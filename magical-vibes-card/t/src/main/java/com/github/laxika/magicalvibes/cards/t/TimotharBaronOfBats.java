package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.CreateTokenLinkedToCardsExiledThisWayEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.AllOfCost;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCC", collectorNumber = "210")
@CardRegistration(set = "VOC", collectorNumber = "4")
@CardRegistration(set = "VOC", collectorNumber = "42")
public class TimotharBaronOfBats extends Card {

    public TimotharBaronOfBats() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), UnlessPaysEffect.counterUnlessDiscards());
        addTrigger(Triggers.nontokenCreatureDies(
                        new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                UnlessPaysEffect.youMayPay(AllOfCost.of(new PayManaCost("{1}"),
                                new ExileCardsCost(Subjects.triggeringCards())),
                        new CreateTokenLinkedToCardsExiledThisWayEffect(batToken()))
                        .withPrompt("Pay {1} and exile that card to create a Bat?"));
    }

    private static CreateTokenEffect batToken() {
        return new CreateTokenEffect(
                1,
                "Bat",
                1,
                1,
                CardColor.BLACK,
                List.of(CardSubtype.BAT),
                Set.of(Keyword.FLYING),
                Set.<CardType>of(),
                List.of(
                        new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(), SequenceEffect.of(
                                new SacrificeSelfEffect(),
                                new PutCardsOntoBattlefieldEffect(Subjects.exiledWithSource())
                                        .enteringTapped()
                                        .underControlOf(Subjects.you())))));
    }
}
