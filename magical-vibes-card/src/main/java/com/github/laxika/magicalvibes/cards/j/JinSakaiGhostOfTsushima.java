package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttacksPlayerAlone;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2226")
public class JinSakaiGhostOfTsushima extends Card {

    public JinSakaiGhostOfTsushima() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardEffect());
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new ConditionalEffect(new AttacksPlayerAlone(),
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Standoff — It gains double strike until end of turn",
                                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TRIGGERING_PERMANENT)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Ghost — It can't be blocked this turn",
                                        new MakeCreatureUnblockableEffect())
                        ))));
    }
}
