package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TibaltCosmicImpostor;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentRevealsHandAndExilesCreatureCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "114")
@CardRegistration(set = "KHM", collectorNumber = "286")
@CardRegistration(set = "KHM", collectorNumber = "308")
public class ValkiGodOfLies extends Card {

    public ValkiGodOfLies() {
        setBackFaceCard(new TibaltCosmicImpostor());
        setModalDoubleFaced(true);

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new EachOpponentRevealsHandAndExilesCreatureCardEffect());
        addActivatedAbility(new ActivatedAbility(
                false, "{X}", List.of(SequenceEffect.of(
                        ChooseCardEffect.oneOf(Subjects.exiledWithSourceUnlinked(new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE), new CardManaValueEqualsXPredicate()))),
                                "a creature card exiled with Valki with mana value X"),
                        BecomeCopyOfSubjectEffect.of(Subjects.cardsThisWay(StepKind.CHOOSE)))),
                "{X}: Choose a creature card exiled with Valki with mana value X. Valki becomes a copy of that card."
        ));
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Valki, God of Lies", List.of())
                        .withManaCost("{1}{B}"),
                new ChooseOneEffect.ChooseOneOption("Tibalt, Cosmic Impostor", List.of())
                        .withManaCost("{5}{B}{R}")
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "TibaltCosmicImpostor";
    }
}
