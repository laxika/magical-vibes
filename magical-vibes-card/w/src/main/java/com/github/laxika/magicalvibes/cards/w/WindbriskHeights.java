package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.AttackedWithCreaturesThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayImprintedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "281")
@CardRegistration(set = "MD1", collectorNumber = "18")
@CardRegistration(set = "V12", collectorNumber = "15")
@CardRegistration(set = "SLD", collectorNumber = "436")
@CardRegistration(set = "PIP", collectorNumber = "315")
@CardRegistration(set = "PIP", collectorNumber = "527")
@CardRegistration(set = "PIP", collectorNumber = "843")
@CardRegistration(set = "PIP", collectorNumber = "1055")
@CardRegistration(set = "LTC", collectorNumber = "345")
@CardRegistration(set = "C20", collectorNumber = "321")
@CardRegistration(set = "C16", collectorNumber = "336")
@CardRegistration(set = "ONC", collectorNumber = "174")
public class WindbriskHeights extends Card {

    public WindbriskHeights() {
        // Hideaway 4 — when this enters, look at the top four cards, exile one face down, rest on bottom.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ImprintFromTopCardsEffect(4, true));
        // This land enters tapped.
        addStaticEffect(new EntersTappedEffect());
        // {W}, {T}: You may play the exiled card without paying its mana cost if you attacked with three or more creatures this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(new ConditionalEffect(new AttackedWithCreaturesThisTurn(3),
                        new PlayImprintedCardWithoutPayingManaCostEffect())),
                "{W}, {T}: You may play the exiled card without paying its mana cost if you attacked with three or more creatures this turn."
        ));
        // {T}: Add {W}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
    }
}
