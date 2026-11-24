package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ControllerSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.Set;

@CardRegistration(set = "GPT", collectorNumber = "90")
public class LeylineOfLifeforce extends Card {

    public LeylineOfLifeforce() {
        addStaticEffect(new MayEffect(
                new LeylineStartOnBattlefieldEffect(),
                "Begin the game with Leyline of Lifeforce on the battlefield?"
        ));
        addStaticEffect(new ControllerSpellsCantBeCounteredEffect(Subjects.eachPlayer(), Set.of(CardType.CREATURE), null));
    }
}
