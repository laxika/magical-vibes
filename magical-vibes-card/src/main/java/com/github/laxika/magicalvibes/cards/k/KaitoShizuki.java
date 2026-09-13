package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardUnlessAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;
import com.github.laxika.magicalvibes.model.effect.SearchBlueOrBlackCreatureToBattlefieldOnAllyCombatDamageEffect;
import com.github.laxika.magicalvibes.model.condition.SourceEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.ActivatedAbility;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "226")
public class KaitoShizuki extends Card {

    public KaitoShizuki() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new SourceEnteredBattlefieldThisTurn(),
                        new PhaseOutEffect(PhaseOutSubject.SOURCE)));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DrawCardEffect(1), new DiscardCardUnlessAttackedThisTurnEffect()),
                "+1: Draw a card. Then discard a card unless you attacked this turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenEffect(
                        1, "Ninja", 1, 1, CardColor.BLUE, List.of(CardSubtype.NINJA),
                        Set.of(), Set.of(), Map.of(EffectSlot.STATIC, new CantBeBlockedEffect()))),
                "−2: Create a 1/1 blue Ninja creature token with \"This token can't be blocked.\""
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateEmblemEffect(
                        List.of(new SearchBlueOrBlackCreatureToBattlefieldOnAllyCombatDamageEffect.Marker()),
                        "Whenever a creature you control deals combat damage to a player, search your library for a blue or black creature card, put it onto the battlefield, then shuffle.")),
                "−7: You get an emblem with \"Whenever a creature you control deals combat damage to a player, search your library for a blue or black creature card, put it onto the battlefield, then shuffle.\""
        ));
    }
}
