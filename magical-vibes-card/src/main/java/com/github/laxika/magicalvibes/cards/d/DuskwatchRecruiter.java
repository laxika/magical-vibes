package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.k.KrallenhordeHowler;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "193")
public class DuskwatchRecruiter extends Card {

    public DuskwatchRecruiter() {
        // Set up back face
        KrallenhordeHowler backFace = new KrallenhordeHowler();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // {2}{G}: Look at the top three cards of your library. You may reveal a creature card from among them
        // and put it into your hand. Put the rest on the bottom of your library in any order.
        addActivatedAbility(new ActivatedAbility(
                false, "{2}{G}",
                List.of(LookAtTopCardsEffect.mayRevealOneToHandRestOnBottom(3, new CardTypePredicate(CardType.CREATURE))),
                "{2}{G}: Look at the top three cards of your library. You may reveal a creature card from among them and put it into your hand. Put the rest on the bottom of your library in any order."
        ));

        // At the beginning of each upkeep, if no spells were cast last turn, transform this creature.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ConditionalEffect(new NoSpellsCastLastTurn(), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "KrallenhordeHowler";
    }
}
