package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsMonstrous;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "JOU", collectorNumber = "128")
public class HydraBroodmaster extends Card {

    public HydraBroodmaster() {
        SourceIsMonstrous monstrous = new SourceIsMonstrous();

        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{X}{G}",
                List.of(new MonstrosityEffect(new XValue())),
                "{X}{X}{G}: Monstrosity X."
        ).withActivationCondition(new NotCondition(monstrous), "This creature is already monstrous"));

        addEffect(EffectSlot.ON_SELF_BECOMES_MONSTROUS, new CreateTokenEffect(
                CardType.CREATURE,
                new XValue(),
                "Hydra",
                new XValue(),
                new XValue(),
                CardColor.GREEN,
                null,
                List.of(CardSubtype.HYDRA),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of()));
    }
}
