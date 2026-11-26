package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.ThroneOfDeath;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "92")
@CardRegistration(set = "KHM", collectorNumber = "306")
public class EgonGodOfDeath extends Card {

    public EgonGodOfDeath() {
        setBackFaceCard(new ThroneOfDeath());
        setModalDoubleFaced(true);

        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new ConditionalEffect(new GraveyardCardThreshold(2, null),
                        ExileGraveyardCardsEffect.fromOwnGraveyard(2, null)),
                new ConditionalEffect(new NotCondition(HappenedThisWay.exiled()),
                        SequenceEffect.of(new SacrificeSelfEffect(), new DrawCardEffect(1)))));
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Egon, God of Death", List.of())
                        .withManaCost("{2}{B}"),
                new ChooseOneEffect.ChooseOneOption("Throne of Death", List.of())
                        .withManaCost("{B}")
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "ThroneOfDeath";
    }
}
