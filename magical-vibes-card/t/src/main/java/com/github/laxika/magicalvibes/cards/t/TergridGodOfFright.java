package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "112")
@CardRegistration(set = "KHM", collectorNumber = "307")
public class TergridGodOfFright extends Card {

    public TergridGodOfFright() {
        setBackFaceCard(new TergridsLantern());
        setModalDoubleFaced(true);

        MayEffect returnSacrificedPermanent = new MayEffect(
                new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(false, true),
                "Put that card onto the battlefield under your control?");
        addTrigger(Triggers.sacrificed(Triggers.NONTOKEN, ObjectRelation.OPPONENT_CONTROLS), returnSacrificedPermanent);

        addTrigger(Triggers.opponentDiscardsCard()
                .withFilter(new CardAllOfPredicate(List.of(
                        new CardIsPermanentPredicate(),
                        new CardNotPredicate(new CardIsTokenPredicate())))),
                returnSacrificedPermanent);

        addSpellEffect(new ChooseOneEffect(List.of(
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
