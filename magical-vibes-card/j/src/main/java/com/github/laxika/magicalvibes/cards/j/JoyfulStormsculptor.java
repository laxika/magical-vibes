package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentProtectedByOpponentOfSourceControllerPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "243")
public class JoyfulStormsculptor extends Card {

    public JoyfulStormsculptor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(2, "Elemental", 1, 1, null,
                        Set.of(CardColor.BLUE, CardColor.RED), List.of(CardSubtype.ELEMENTAL)));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardKeywordPredicate(Keyword.CONVOKE),
                List.of(
                        new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT),
                        new DealDamageToEachMatchingPermanentEffect(1,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsBattlePredicate(),
                                        new PermanentProtectedByOpponentOfSourceControllerPredicate())),
                                EachPermanentScope.ALL_PLAYERS))));
    }
}
