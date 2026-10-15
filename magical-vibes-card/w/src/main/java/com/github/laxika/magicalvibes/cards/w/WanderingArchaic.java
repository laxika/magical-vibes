package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.ExploreTheVastlands;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "6")
public class WanderingArchaic extends Card {

    public WanderingArchaic() {
        ExploreTheVastlands backFace = new ExploreTheVastlands();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Wandering Archaic", List.of()),
                new ChooseOneEffect.ChooseOneOption(
                        "Explore the Vastlands", backFace.getEffects(EffectSlot.SPELL)).withManaCost("{3}")
        )));
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                List.of(new MayPayManaEffect(
                        "{2}",
                        null,
                        "Pay {2} to avoid copying that spell?",
                        MayPayPayer.TRIGGERING_SPELL_CONTROLLER,
                        new MayEffect(new CopySpellEffect(), "Copy that spell?"),
                        0
                ))));
    }

    @Override
    public String getBackFaceClassName() {
        return "ExploreTheVastlands";
    }
}
