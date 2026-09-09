package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "118")
public class UnholyAnnexRitualChamber extends Card {

    public UnholyAnnexRitualChamber() {
        setRoomDoorManaCosts(List.of("{2}{B}", "{3}{B}{B}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Unholy Annex", List.of())
                        .withManaCost("{2}{B}"),
                new ChooseOneEffect.ChooseOneOption("Ritual Chamber", List.of())
                        .withManaCost("{3}{B}{B}")
        )));

        var controlsDemon = new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.DEMON));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, SequenceEffect.of(
                new DrawCardEffect(),
                new ConditionalEffect(controlsDemon, SequenceEffect.of(
                        new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(2))),
                new ConditionalEffect(new NotCondition(controlsDemon), new LoseLifeEffect(2))));

        CreateTokenEffect demonToken = new CreateTokenEffect(
                "Demon", 6, 6, CardColor.BLACK, List.of(CardSubtype.DEMON),
                Set.of(Keyword.FLYING), Set.of());
        addEffect(EffectSlot.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggeringRoomDoorConditionalEffect(1, demonToken));
    }
}
