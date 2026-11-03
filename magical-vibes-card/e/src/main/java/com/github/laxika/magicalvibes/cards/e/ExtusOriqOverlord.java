package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "149")
public class ExtusOriqOverlord extends Card {

    public ExtusOriqOverlord() {
        AwakenTheBloodAvatar backFace = new AwakenTheBloodAvatar();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        ReturnCardFromGraveyardEffect returnCreature = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardNotPredicate(new CardSupertypePredicate(CardSupertype.LEGENDARY)))))
                .targetGraveyard(true)
                .build();
        addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery), returnCreature);
        addTrigger(Triggers.youCopySpell(),
                new SpellCopyTriggerEffect(instantOrSorcery, List.of(returnCreature)));

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Extus, Oriq Overlord", List.of()),
                new ChooseOneEffect.ChooseOneOption(
                        "Awaken the Blood Avatar", backFace.getSpellEffects()))));
    }

    @Override
    public String getBackFaceClassName() {
        return "AwakenTheBloodAvatar";
    }
}
