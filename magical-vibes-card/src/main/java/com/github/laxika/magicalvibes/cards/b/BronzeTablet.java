package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BronzeTabletAnteExchangeEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "303")
public class BronzeTablet extends Card {

    public BronzeTablet() {
        // This artifact enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {4}, {T}: Exile this artifact and target nontoken permanent an opponent owns. That player
        // may pay 10 life. If they do, put this card into its owner's graveyard. Otherwise, that
        // player owns this card and you own the other exiled card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new BronzeTabletAnteExchangeEffect(10)),
                "{4}, {T}: Exile Bronze Tablet and target nontoken permanent an opponent owns. That "
                        + "player may pay 10 life. If they do, put this card into its owner's graveyard. "
                        + "Otherwise, that player owns this card and you own the other exiled card.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                                new PermanentNotPredicate(new PermanentOwnedBySourceControllerPredicate())
                        )),
                        "Target must be a nontoken permanent an opponent owns"
                )
        ));
    }
}
