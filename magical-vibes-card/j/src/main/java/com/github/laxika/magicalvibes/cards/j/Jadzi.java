package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayCastForManaOrPutLandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "151")
public class Jadzi extends Card {

    public Jadzi() {
        JourneyToTheOracle backFace = new JourneyToTheOracle();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        List<CardEffect> magecraft = List.of(
                new RevealTopCardMayCastForManaOrPutLandOntoBattlefieldEffect("{1}"));
        addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery), magecraft.toArray(CardEffect[]::new));
        addTrigger(Triggers.youCopySpell(),
                new SpellCopyTriggerEffect(instantOrSorcery, magecraft));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new DiscardCardTypeCost(null, null), ReturnToHandEffect.self()),
                "Discard a card: Return Jadzi, Oracle of Arcavios to its owner's hand."
        ));

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Jadzi, Oracle of Arcavios", List.of()),
                new ChooseOneEffect.ChooseOneOption(
                        "Journey to the Oracle", backFace.getSpellEffects()))));
    }

    @Override
    public String getBackFaceClassName() {
        return "JourneyToTheOracle";
    }
}
