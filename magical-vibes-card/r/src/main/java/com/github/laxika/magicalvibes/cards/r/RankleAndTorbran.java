package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "252")
@CardRegistration(set = "MOM", collectorNumber = "315")
public class RankleAndTorbran extends Card {

    public RankleAndTorbran() {
        addTrigger(Triggers.dealsCombatDamageToPlayerOrBattle(),
                new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("Each player creates a Treasure token.",
                                new EachPlayerCreatesTokenEffect(CreateTokenEffect.ofTreasureToken(1))),
                        new ChooseOneEffect.ChooseOneOption("Each player sacrifices a creature of their choice.",
                                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                                        Subjects.eachPlayer()).withSimultaneousChoices()),
                        new ChooseOneEffect.ChooseOneOption(
                                "If a source would deal damage to a player or battle this turn, it deals that much damage plus 2 instead.",
                                GrantStaticEffectToPlayerEffect.untilEndOfTurn(
                                        new AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(2)
                                                .fromAnyonesSources()
                                                .to(Subjects.eachPlayer(), Subjects.controlledBy(
                                                        Subjects.eachPlayer(), new PermanentIsBattlePredicate()))))
                ), true, 0, 3)));
    }
}
