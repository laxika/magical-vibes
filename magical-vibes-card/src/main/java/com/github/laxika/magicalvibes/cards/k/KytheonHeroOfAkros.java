package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GideonBattleForged;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAtEndOfCombatAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "23")
public class KytheonHeroOfAkros extends Card {

    public KytheonHeroOfAkros() {
        setBackFaceCard(new GideonBattleForged());

        // At end of combat, if Kytheon and at least two other creatures attacked this combat, exile
        // Kytheon, then return him to the battlefield transformed under his owner's control.
        // The attacker count is fixed at declare attackers, so this rides the battalion trigger and
        // schedules the exile-and-return for end of combat.
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new MinimumAttackers(3),
                new ExileSelfAtEndOfCombatAndReturnTransformedEffect()));

        // {2}{W}: Kytheon gains indestructible until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{2}{W}",
                List.of(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)),
                "{2}{W}: Kytheon, Hero of Akros gains indestructible until end of turn."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "GideonBattleForged";
    }
}
