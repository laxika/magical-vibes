package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1314")
public class MuxusGoblinGrandee extends Card {

    public MuxusGoblinGrandee() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new LookAtTopCardsEffect(
                        new Fixed(6), new Fixed(6),
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSubtypePredicate(CardSubtype.GOBLIN),
                                new CardMaxManaValuePredicate(5))),
                        LookDestination.BOTTOM_OF_LIBRARY_RANDOM,
                        true, LibrarySearchDestination.BATTLEFIELD, false,
                        false, null, null, false, 0, true));

        PermanentCount otherGoblins = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN), CountScope.CONTROLLER, true);
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(otherGoblins, otherGoblins));
    }
}
