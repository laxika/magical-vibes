package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.AttackedWithCreaturesOfSubtypeThisTurn;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "42")
public class RahildaWantedCutthroat extends Card {

    public RahildaWantedCutthroat() {
        setBackFaceCard(new RahildaFeralOutlaw());
        addTrigger(Triggers.dealsCombatDamageToPlayer(), exileNonlandCardAtRandom());
    }

    @Override
    public String getBackFaceClassName() {
        return "RahildaFeralOutlaw";
    }

    /**
     * "Exile a nonland card from their library at random. During any turn you attacked with a Wolf or Werewolf, you
     * may cast that card and you may spend mana as though it were mana of any color to cast that spell."
     */
    static CardEffect exileNonlandCardAtRandom() {
        return SequenceEffect.of(
                ExileCardAtRandomEffect.fromLibrary(Subjects.triggeringPlayer(), ExileCardsFromTopUntilEffect.NONLAND),
                AllowPlayCardsFromExileEffect.whileExiled(Subjects.cardsThisWay(StepKind.EXILE))
                        .withAnyManaType()
                        .playableOnlyIf(new AnyOf(List.of(
                                new AttackedWithCreaturesOfSubtypeThisTurn(1, CardSubtype.WOLF),
                                new AttackedWithCreaturesOfSubtypeThisTurn(1, CardSubtype.WEREWOLF)))));
    }
}
