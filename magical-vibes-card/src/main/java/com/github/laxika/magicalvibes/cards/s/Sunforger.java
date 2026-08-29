package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.UnattachSourceEquipmentCost;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "272")
public class Sunforger extends Card {

    public Sunforger() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(4, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        false,
                        "{R}{W}",
                        List.of(
                                new UnattachSourceEquipmentCost(),
                                new SearchLibraryEffect(
                                        new CardAllOfPredicate(List.of(
                                                new CardAnyOfPredicate(List.of(
                                                        new CardColorPredicate(CardColor.RED),
                                                        new CardColorPredicate(CardColor.WHITE))),
                                                new CardTypePredicate(CardType.INSTANT))),
                                        LibrarySearchDestination.CAST_WITHOUT_PAYING,
                                        new ManaValueBound(new Fixed(4), false, 0))),
                        "{R}{W}, Unattach Sunforger: Search your library for a red or white instant card "
                                + "with mana value 4 or less and cast that card without paying its mana cost. "
                                + "Then shuffle."
                ),
                GrantScope.EQUIPPED_CREATURE
        ));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
