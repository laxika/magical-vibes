package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "112")
public class TergridGodOfFright extends Card {

    public TergridGodOfFright() {
        setBackFaceCard(new TergridsLantern());
        setModalDoubleFaced(true);

        MayEffect returnSacrificedPermanent = new MayEffect(
                new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(false, true),
                "Put that card onto the battlefield under your control?");
        addEffect(EffectSlot.ON_OPPONENT_NONTOKEN_PERMANENT_SACRIFICED, returnSacrificedPermanent);

        addEffect(EffectSlot.ON_OPPONENT_DISCARDS, new TriggeringCardConditionalEffect(
                new CardAllOfPredicate(List.of(
                        new CardIsPermanentPredicate(),
                        new CardNotPredicate(new CardIsTokenPredicate()))),
                returnSacrificedPermanent));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Tergrid, God of Fright", List.of())
                        .withManaCost("{3}{B}{B}"),
                new ChooseOneEffect.ChooseOneOption("Tergrid's Lantern", List.of())
                        .withManaCost("{3}{B}")
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "TergridsLantern";
    }
}
