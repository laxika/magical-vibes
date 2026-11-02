package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "348")
public class MyWingsEnfoldAll extends Card {

    public MyWingsEnfoldAll() {
        var instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Draw two cards",
                        new DrawCardEffect(2)),
                new ChooseOneEffect.ChooseOneOption(
                        "Until end of turn, whenever you cast an instant or sorcery spell, copy it. You may choose new targets for the copy",
                        new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                                new TriggeredAbility(Triggers.youCastSpell(),
                                new CopyControllerCastSpellOnSpellCastEffect(instantOrSorcery, null, null)))))));
    }
}
