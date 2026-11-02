package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MoonscarredWerewolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "125")
@CardRegistration(set = "INR", collectorNumber = "212")
@CardRegistration(set = "INR", collectorNumber = "468")
public class ScornedVillager extends Card {

    public ScornedVillager() {
        setBackFaceCard(new MoonscarredWerewolf());

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));

        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new ConditionalEffect(new NoSpellsCastLastTurn(), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "MoonscarredWerewolf";
    }
}
