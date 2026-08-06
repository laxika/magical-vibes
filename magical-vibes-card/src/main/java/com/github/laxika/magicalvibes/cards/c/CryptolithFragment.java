package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AuroraOfEmrakul;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.EachPlayerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "260")
@CardRegistration(set = "INR", collectorNumber = "472")
public class CryptolithFragment extends Card {

    public CryptolithFragment() {
        setBackFaceCard(new AuroraOfEmrakul());

        // This artifact enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add one mana of any color. Each player loses 1 life.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(
                        new AwardAnyColorManaEffect(),
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_PLAYER)
                ),
                "{T}: Add one mana of any color. Each player loses 1 life."
        ));

        // At the beginning of your upkeep, if each player has 10 or less life, transform this artifact.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(new EachPlayerLifeAtMost(10), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "AuroraOfEmrakul";
    }
}
