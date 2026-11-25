package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "198")
@CardRegistration(set = "ELD", collectorNumber = "382")
@CardRegistration(set = "OTP", collectorNumber = "51")
public class OutlawsMerriment extends Card {

    public OutlawsMerriment() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ChooseOneAtRandomEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Human Warrior",
                        token("Human Warrior", 3, 1, List.of(CardSubtype.HUMAN, CardSubtype.WARRIOR),
                                Set.of(Keyword.TRAMPLE, Keyword.HASTE), List.of())),
                new ChooseOneEffect.ChooseOneOption("Human Cleric",
                        token("Human Cleric", 2, 1, List.of(CardSubtype.HUMAN, CardSubtype.CLERIC),
                                Set.of(Keyword.LIFELINK, Keyword.HASTE), List.of())),
                new ChooseOneEffect.ChooseOneOption("Human Rogue",
                        token("Human Rogue", 1, 2, List.of(CardSubtype.HUMAN, CardSubtype.ROGUE),
                                Set.of(Keyword.HASTE),
                                List.of(new TriggeredAbility(Triggers.enters(ObjectRelation.THIS),
                                        DealDamageEffect.toAnyTarget(1))))))));
    }

    private static CreateTokenEffect token(String name, int power, int toughness,
                                           List<CardSubtype> subtypes, Set<Keyword> keywords,
                                           List<Ability> tokenEffects) {
        return new CreateTokenEffect(
                CardType.CREATURE,
                1,
                name,
                power,
                toughness,
                null,
                Set.of(CardColor.RED, CardColor.WHITE),
                subtypes,
                keywords,
                Set.of(),
                false,
                false,
                tokenEffects,
                List.of(),
                false,
                false,
                false,
                0,
                Set.of());
    }
}
