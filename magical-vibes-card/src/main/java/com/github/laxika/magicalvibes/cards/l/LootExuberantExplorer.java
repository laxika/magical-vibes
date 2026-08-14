package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "106")
public class LootExuberantExplorer extends Card {

    public LootExuberantExplorer() {
        addEffect(EffectSlot.STATIC, new PlaysAdditionalLandEachTurnEffect(1));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{G}{G}",
                List.of(LookAtTopCardsEffect.mayPutMatchingOntoBattlefieldRestOnBottomRandom(
                        6,
                        new CardTypePredicate(CardType.CREATURE),
                        new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER))),
                "{4}{G}{G}, {T}: Look at the top six cards of your library. You may reveal a creature "
                        + "card with mana value less than or equal to the number of lands you control "
                        + "from among them and put it onto the battlefield. Put the rest on the bottom "
                        + "in a random order."));
    }
}
