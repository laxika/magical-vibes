package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.ExploreTheVastlands;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "6")
public class WanderingArchaic extends Card {

    public WanderingArchaic() {
        ExploreTheVastlands backFace = new ExploreTheVastlands();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Wandering Archaic", List.of()),
                new ChooseOneEffect.ChooseOneOption(
                        "Explore the Vastlands", backFace.getSpellEffects()).withManaCost("{3}")
        )));
        addTrigger(Triggers.opponentCastsSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)))),
                new MayPayManaEffect(
                        "{2}",
                        null,
                        "Pay {2} to avoid copying that spell?",
                        MayPayPayer.TRIGGERING_SPELL_CONTROLLER,
                        new MayEffect(new CopySpellEffect(), "Copy that spell?"),
                        0
                ));
    }

    @Override
    public String getBackFaceClassName() {
        return "ExploreTheVastlands";
    }
}
