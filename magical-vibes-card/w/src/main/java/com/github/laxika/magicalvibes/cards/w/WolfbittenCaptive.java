package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.k.KrallenhordeKiller;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "133")
public class WolfbittenCaptive extends Card {

    public WolfbittenCaptive() {
        setBackFaceCard(new KrallenhordeKiller());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new BoostSelfEffect(2, 2)),
                "{1}{G}: This creature gets +2/+2 until end of turn. Activate only once each turn.",
                1
        ));

        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new ConditionalEffect(new NoSpellsCastLastTurn(), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "KrallenhordeKiller";
    }
}
